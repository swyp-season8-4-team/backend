#!/bin/bash

# 서비스 헬스체크 스크립트
# 사용법:
#   ./health-check.sh <서비스명> [타임아웃]
#   ./health-check.sh all [타임아웃]
#   ./health-check.sh --config <설정파일>

set -e

# 기본 설정
DEFAULT_TIMEOUT=60
HEALTH_CHECK_CONFIG="health-check.conf"

# 색상 출력
print_status() {
    local status=$1
    local message=$2
    case $status in
        "OK")
            echo -e "\033[32m✓ $message\033[0m"
            ;;
        "FAIL")
            echo -e "\033[31m✗ $message\033[0m"
            ;;
        "WARN")
            echo -e "\033[33m⚠ $message\033[0m"
            ;;
        "INFO")
            echo -e "\033[36m• $message\033[0m"
            ;;
    esac
}

# 기본 서비스 설정 (설정 파일이 없을 때 사용)
init_default_config() {
    cat > "$HEALTH_CHECK_CONFIG" <<EOF
# 헬스체크 설정 파일
# 형식: SERVICE_NAME:CONTAINER_NAME:CHECK_TYPE:CHECK_COMMAND:PORT

# 핵심 서비스
app:desserbee-app:http:/actuator/health:8080
nginx:desserbee-nginx:container::
redis:desserbee-redis:redis:ping:6379
EOF
    print_status "INFO" "기본 설정 파일 생성: $HEALTH_CHECK_CONFIG"
}

# 설정 파일 로드
load_config() {
    if [ ! -f "$HEALTH_CHECK_CONFIG" ]; then
        print_status "WARN" "설정 파일이 없습니다. 기본 설정을 생성합니다."
        init_default_config
    fi

    # 주석과 빈 줄 제거하여 서비스 목록 추출
    SERVICES=($(grep -v '^#' "$HEALTH_CHECK_CONFIG" | grep -v '^$'))
}

# 컨테이너 상태 확인
check_container_status() {
    local service_name=$1

    local ps_output=$(docker-compose ps "$service_name" 2>/dev/null)

    # 서비스가 없으면 실패
    if [ -z "$ps_output" ] || ! echo "$ps_output" | grep -q "$service_name"; then
        return 1
    fi

    # STATUS 컬럼(6번째)에서 상태 추출
    # "Up 5 minutes" 또는 "Up 46 hours (healthy)" 형태
    local status_info=$(echo "$ps_output" | grep "$service_name" | awk '{print $6, $7, $8, $9}' | head -1)

    # State가 "Up"으로 시작하고 "unhealthy"가 포함되지 않은 경우만 정상
    if [[ "$status_info" =~ ^Up ]]; then
        # "unhealthy"가 포함되어 있는지 확인
        if [[ "$status_info" =~ unhealthy ]]; then
            return 1  # unhealthy
        else
            return 0  # 정상
        fi
    else
        return 1  # Up이 아님
    fi
}

# 개별 서비스 헬스체크
check_service() {
    local service_config=$1
    local timeout=${2:-$DEFAULT_TIMEOUT}

    # 설정 파싱
    IFS=':' read -r service_name container_name check_type check_command port <<< "$service_config"

    print_status "INFO" "[$service_name] 헬스체크 시작 (timeout: ${timeout}s)"

    local service_healthy=false

    for i in $(seq 1 $timeout); do
        # 1. 컨테이너 상태 확인
        if ! check_container_status "$service_name"; then
            if [ $i -eq 1 ]; then
                print_status "INFO" "[$service_name] 컨테이너 시작 대기 중..."
            fi
            if [ $i -eq $timeout ]; then
                # 텍스트 파싱으로 상태 정보 추출
                local ps_output=$(docker-compose ps "$service_name" 2>/dev/null)
                if [ -z "$ps_output" ] || ! echo "$ps_output" | grep -q "$service_name"; then
                    print_status "FAIL" "[$service_name] 컨테이너를 찾을 수 없습니다"
                else
                    local status_info=$(echo "$ps_output" | grep "$service_name" | awk '{print $6, $7, $8, $9}' | head -1)
                    print_status "FAIL" "[$service_name] 컨테이너 상태 비정상: $status_info"
                fi
                return 1
            fi
            sleep 3
            continue
        fi

        # 2. 서비스별 헬스체크
        case "$check_type" in
            "http")
                # HTTP 헬스체크
                if docker-compose exec -T "$service_name" curl -f "http://localhost:${port}${check_command}" >/dev/null 2>&1; then
                    service_healthy=true
                elif [ $i -eq $timeout ]; then
                    print_status "FAIL" "[$service_name] HTTP 헬스체크 실패: http://localhost:${port}${check_command}"
                    return 1
                fi
                ;;
            "redis")
                # Redis 헬스체크
                if docker-compose exec -T "$service_name" redis-cli ping | grep -q "PONG" >/dev/null 2>&1; then
                    service_healthy=true
                elif [ $i -eq $timeout ]; then
                    print_status "FAIL" "[$service_name] Redis ping 실패"
                    return 1
                fi
                ;;
            "container")
                # 컨테이너 상태만 확인 (이미 위에서 확인됨)
                service_healthy=true
                ;;
            "custom")
                # 커스텀 명령어 실행
                if eval "$check_command" >/dev/null 2>&1; then
                    service_healthy=true
                elif [ $i -eq $timeout ]; then
                    print_status "FAIL" "[$service_name] 커스텀 헬스체크 실패: $check_command"
                    return 1
                fi
                ;;
            *)
                print_status "WARN" "[$service_name] 알 수 없는 헬스체크 타입: $check_type"
                service_healthy=true  # 컨테이너 상태만으로 판단
                ;;
        esac

        if [ "$service_healthy" = true ]; then
            print_status "OK" "[$service_name] 서비스 정상"
            return 0
        fi

        if [ $i -lt $timeout ]; then
            sleep 3
        fi
    done

    print_status "FAIL" "[$service_name] 헬스체크 타임아웃"
    return 1
}

# 모든 서비스 헬스체크
check_all_services() {
    local timeout=${1:-$DEFAULT_TIMEOUT}
    local failed_services=()
    local total_services=0

    print_status "INFO" "=== 전체 서비스 헬스체크 시작 ==="

    for service_config in "${SERVICES[@]}"; do
        total_services=$((total_services + 1))
        service_name=$(echo "$service_config" | cut -d':' -f1)

        if ! check_service "$service_config" "$timeout"; then
            failed_services+=("$service_name")
        fi
        echo ""
    done

    print_status "INFO" "=== 헬스체크 결과 요약 ==="
    print_status "INFO" "총 서비스 수: $total_services"
    print_status "INFO" "성공: $((total_services - ${#failed_services[@]}))"
    print_status "INFO" "실패: ${#failed_services[@]}"

    if [ ${#failed_services[@]} -eq 0 ]; then
        print_status "OK" "모든 서비스가 정상 작동합니다"
        return 0
    else
        print_status "FAIL" "실패한 서비스: ${failed_services[*]}"
        return 1
    fi
}

# 서비스 목록 출력
list_services() {
    load_config
    echo "설정된 서비스 목록:"
    echo "===================="
    for service_config in "${SERVICES[@]}"; do
        IFS=':' read -r service_name container_name check_type check_command port <<< "$service_config"
        echo "서비스: $service_name"
        echo "  컨테이너: $container_name"
        echo "  헬스체크: $check_type"
        echo "  명령어: $check_command"
        echo "  포트: $port"
        echo ""
    done
}

# 사용법 출력
show_usage() {
    echo "사용법: $0 [OPTIONS] <서비스명|all> [타임아웃]"
    echo ""
    echo "옵션:"
    echo "  --config <파일>    설정 파일 지정 (기본: health-check.conf)"
    echo "  --list            설정된 서비스 목록 출력"
    echo "  --init            기본 설정 파일 생성"
    echo "  --help            도움말 출력"
    echo ""
    echo "예시:"
    echo "  $0 app              # app 서비스만 헬스체크"
    echo "  $0 all              # 모든 서비스 헬스체크"
    echo "  $0 all 120          # 120초 타임아웃으로 모든 서비스 헬스체크"
    echo "  $0 --list           # 설정된 서비스 목록 출력"
    echo ""
    echo "설정 파일 형식 (health-check.conf):"
    echo "  서비스명:컨테이너명:체크타입:체크명령어:포트"
    echo ""
    echo "지원하는 체크 타입:"
    echo "  container  - 컨테이너 상태만 확인"
    echo "  http       - HTTP 헬스체크"
    echo "  redis      - Redis ping"
    echo "  custom     - 커스텀 명령어 실행"
}

# 메인 실행부
main() {
    # 옵션 파싱
    while [[ $# -gt 0 ]]; do
        case $1 in
            --config)
                HEALTH_CHECK_CONFIG="$2"
                shift 2
                ;;
            --list)
                load_config
                list_services
                exit 0
                ;;
            --init)
                init_default_config
                exit 0
                ;;
            --help)
                show_usage
                exit 0
                ;;
            -*)
                echo "알 수 없는 옵션: $1"
                show_usage
                exit 1
                ;;
            *)
                break
                ;;
        esac
    done

    # 인자 확인
    if [ $# -eq 0 ]; then
        show_usage
        exit 1
    fi

    local target_service=$1
    local timeout=${2:-$DEFAULT_TIMEOUT}

    # 설정 로드
    load_config

    if [ "$target_service" = "all" ]; then
        check_all_services "$timeout"
    else
        # 특정 서비스 검색
        local found=false
        for service_config in "${SERVICES[@]}"; do
            service_name=$(echo "$service_config" | cut -d':' -f1)
            if [ "$service_name" = "$target_service" ]; then
                check_service "$service_config" "$timeout"
                found=true
                break
            fi
        done

        if [ "$found" = false ]; then
            print_status "FAIL" "서비스를 찾을 수 없습니다: $target_service"
            echo ""
            print_status "INFO" "사용 가능한 서비스:"
            for service_config in "${SERVICES[@]}"; do
                service_name=$(echo "$service_config" | cut -d':' -f1)
                echo "  - $service_name"
            done
            exit 1
        fi
    fi
}

# 스크립트 실행
main "$@"