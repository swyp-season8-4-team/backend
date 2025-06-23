#!/bin/bash

# 통합 헬스체크 스크립트
# 사용법:
#   ./health-check.sh <서비스명> [타임아웃]
#   ./health-check.sh all [타임아웃]
#   ./health-check.sh https [도메인] [타임아웃]
#   ./health-check.sh ssl [도메인]

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

# HTTPS 연결 테스트
check_https_connection() {
    local domain=$1
    local timeout=${2:-30}
    
    print_status "INFO" "HTTPS 연결 테스트: https://$domain"
    
    # curl을 사용한 HTTPS 연결 테스트
    local http_code
    http_code=$(timeout "$timeout" curl -f -s -o /dev/null -w "%{http_code}" \
        --connect-timeout 10 \
        --max-time "$timeout" \
        "https://$domain/actuator/health" 2>/dev/null || echo "000")
    
    if [ "$http_code" = "200" ]; then
        print_status "OK" "HTTPS 연결 성공 (HTTP $http_code)"
        return 0
    else
        print_status "FAIL" "HTTPS 연결 실패 (HTTP $http_code)"
        return 1
    fi
}

# SSL 인증서 상태 확인
check_ssl_status() {
    local domain=${1:-"api.desserbee.com"}
    
    print_status "INFO" "=== SSL 인증서 상태 확인: $domain ==="
    
    # certbot으로 인증서 정보 확인
    if command -v certbot &> /dev/null; then
        local cert_info
        cert_info=$(sudo certbot certificates 2>/dev/null | grep -A 10 "$domain" || echo "")
        
        if [ -n "$cert_info" ]; then
            local expiry_line
            expiry_line=$(echo "$cert_info" | grep "VALID" || echo "")
            if [ -n "$expiry_line" ]; then
                local expiry_date
                expiry_date=$(echo "$expiry_line" | grep -oE "[A-Za-z]{3} [0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2} [0-9]{4} [A-Z]{3}")
                if [ -n "$expiry_date" ]; then
                    local expiry_epoch
                    expiry_epoch=$(date -d "$expiry_date" +%s 2>/dev/null || echo "0")
                    local current_epoch
                    current_epoch=$(date +%s)
                    local days_left=$(( (expiry_epoch - current_epoch) / 86400 ))
                    
                    print_status "INFO" "인증서 만료일: $expiry_date"
                    print_status "INFO" "남은 일수: $days_left일"
                    
                    if [ $days_left -gt 30 ]; then
                        print_status "OK" "인증서 상태가 양호합니다."
                    elif [ $days_left -gt 0 ]; then
                        print_status "WARN" "30일 이내 만료 - 갱신이 필요합니다."
                    else
                        print_status "FAIL" "인증서가 만료되었습니다!"
                    fi
                else
                    print_status "FAIL" "인증서 만료일을 확인할 수 없습니다."
                fi
            else
                print_status "FAIL" "유효한 인증서를 찾을 수 없습니다."
            fi
        else
            print_status "FAIL" "도메인 $domain에 대한 인증서를 찾을 수 없습니다."
        fi
        
        # 갱신 테스트
        print_status "INFO" "인증서 갱신 테스트 (dry-run):"
        if sudo certbot renew --dry-run --webroot --webroot-path="/var/www/certbot" --quiet 2>/dev/null; then
            print_status "OK" "인증서 갱신 프로세스가 정상입니다."
        else
            print_status "FAIL" "인증서 갱신 프로세스에 문제가 있습니다."
        fi
    else
        print_status "FAIL" "certbot이 설치되어 있지 않습니다."
    fi
    
    # cron job 확인
    if crontab -l 2>/dev/null | grep -q "renew-ssl.sh"; then
        print_status "OK" "SSL 갱신 cron job이 설정되어 있습니다."
    else
        print_status "FAIL" "SSL 갱신 cron job이 설정되어 있지 않습니다."
    fi
}

# 기본 서비스 설정 (설정 파일이 없을 때 사용)
init_default_config() {
    cat > "$HEALTH_CHECK_CONFIG" <<EOF
# 헬스체크 설정 파일
# 형식: SERVICE_NAME:CONTAINER_NAME:CHECK_TYPE:CHECK_COMMAND:PORT

# 핵심 서비스 - /actuator/health API 사용
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
    mapfile -t SERVICES < <(grep -v '^#' "$HEALTH_CHECK_CONFIG" | grep -v '^$')
}

# 컨테이너 상태 확인
check_container_status() {
    local service_name=$1

    # docker-compose ps 출력 가져오기
    local ps_output
    ps_output=$(docker-compose ps "$service_name" 2>/dev/null)

    # 서비스가 없으면 실패
    if [ -z "$ps_output" ]; then
        return 1
    fi

    # 헤더를 제외하고 실제 컨테이너 정보만 추출
    local container_line
    container_line=$(echo "$ps_output" | grep -v "^NAME\|^----" | grep "$service_name" | head -1)

    if [ -z "$container_line" ]; then
        return 1
    fi

    # "Up"이 포함되어 있으면 정상으로 판단
    if echo "$container_line" | grep -qi "up"; then
        # "unhealthy", "exited", "dead" 등이 포함되어 있으면 비정상
        if echo "$container_line" | grep -qi "unhealthy\|exited\|dead\|restarting"; then
            return 1
        else
            return 0
        fi
    else
        return 1
    fi
}

# 개별 서비스 헬스체크
check_service() {
    local service_config=$1
    local timeout=${2:-$DEFAULT_TIMEOUT}

    # 설정 파싱
    IFS=':' read -r service_name _ check_type check_command port <<< "$service_config"

    print_status "INFO" "[$service_name] 헬스체크 시작 (timeout: ${timeout}s)"

    local service_healthy=false

    for i in $(seq 1 "$timeout"); do
        # 1. 컨테이너 상태 확인
        if ! check_container_status "$service_name"; then
            if [ "$i" -eq 1 ]; then
                print_status "INFO" "[$service_name] 컨테이너 시작 대기 중..."
            fi
            if [ "$i" -eq "$timeout" ]; then
                # 상세한 상태 정보 출력
                local ps_output
                ps_output=$(docker-compose ps "$service_name" 2>/dev/null)
                if [ -z "$ps_output" ]; then
                    print_status "FAIL" "[$service_name] 컨테이너를 찾을 수 없습니다"
                else
                    local status_line
                    status_line=$(echo "$ps_output" | grep -v "^NAME\|^----" | grep "$service_name" | head -1)
                    print_status "FAIL" "[$service_name] 컨테이너 상태 비정상"
                    print_status "INFO" "상세 정보: $status_line"
                fi
                return 1
            fi
            sleep 3
            continue
        fi

        # 2. 서비스별 헬스체크
        case "$check_type" in
            "http")
                # HTTP 헬스체크 - /actuator/health API 사용
                local http_code
                http_code=$(timeout 10 docker-compose exec -T "$service_name" curl -s -o /dev/null -w "%{http_code}" --connect-timeout 5 "http://localhost:${port}${check_command}" 2>/dev/null || echo "000")

                if [ "$http_code" = "200" ]; then
                    service_healthy=true
                elif [ "$i" -eq "$timeout" ]; then
                    print_status "FAIL" "[$service_name] HTTP 헬스체크 실패 (HTTP $http_code): http://localhost:${port}${check_command}"

                    # 디버깅 정보
                    print_status "INFO" "컨테이너 로그 (마지막 5줄):"
                    docker-compose logs --tail=5 "$service_name" 2>/dev/null | sed 's/^/    /' || echo "    로그 가져오기 실패"

                    # 포트 리스닝 확인
                    print_status "INFO" "포트 리스닝 상태:"
                    docker-compose exec -T "$service_name" netstat -tln 2>/dev/null | grep ":${port}" | sed 's/^/    /' || echo "    포트 $port 리스닝 없음"

                    return 1
                fi
                ;;
            "redis")
                # Redis 헬스체크 - docker exec 사용
                if docker exec desserbee-redis redis-cli ping 2>/dev/null | grep -q "PONG"; then
                    service_healthy=true
                elif [ "$i" -eq "$timeout" ]; then
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
                if timeout 10 eval "$check_command" >/dev/null 2>&1; then
                    service_healthy=true
                elif [ "$i" -eq "$timeout" ]; then
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

        if [ "$i" -lt "$timeout" ]; then
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

    # 디버깅을 위한 현재 컨테이너 상태 출력
    print_status "INFO" "현재 컨테이너 상태:"
    docker-compose ps

    load_config

    for service_config in "${SERVICES[@]}"; do
        total_services=$((total_services + 1))
        
        if ! check_service "$service_config" "$timeout"; then
            failed_services+=("$service_config")
        fi
    done

    # 결과 요약
    print_status "INFO" "=== 헬스체크 결과 요약 ==="
    print_status "INFO" "총 서비스 수: $total_services"
    print_status "INFO" "성공: $((total_services - ${#failed_services[@]}))"
    print_status "INFO" "실패: ${#failed_services[@]}"

    if [ ${#failed_services[@]} -eq 0 ]; then
        print_status "OK" "모든 서비스가 정상 작동합니다"
        return 0
    else
        print_status "FAIL" "일부 서비스에 문제가 있습니다"
        for failed_service in "${failed_services[@]}"; do
            IFS=':' read -r service_name _ _ _ _ <<< "$failed_service"
            print_status "FAIL" "- $service_name"
        done
        return 1
    fi
}

# 메인 실행 로직
main() {
    case "$1" in
        "https")
            if [ -z "$2" ]; then
                echo "사용법: $0 https <도메인> [타임아웃]"
                exit 1
            fi
            check_https_connection "$2" "$3"
            ;;
        "ssl")
            check_ssl_status "$2"
            ;;
        "all")
            check_all_services "$2"
            ;;
        "")
            echo "사용법: $0 <서비스명|all|https|ssl> [타임아웃]"
            echo "예시:"
            echo "  $0 all 60"
            echo "  $0 app 30"
            echo "  $0 https api.desserbee.com 30"
            echo "  $0 ssl api.desserbee.com"
            exit 1
            ;;
        *)
            # 개별 서비스 헬스체크
            load_config
            for service_config in "${SERVICES[@]}"; do
                IFS=':' read -r service_name _ _ _ _ <<< "$service_config"
                if [ "$service_name" = "$1" ]; then
                    check_service "$service_config" "$2"
                    exit $?
                fi
            done
            print_status "FAIL" "서비스를 찾을 수 없습니다: $1"
            exit 1
            ;;
    esac
}

# 스크립트 실행
main "$@"