#!/bin/bash

# 애플리케이션 배포 스크립트
# 사용법: ./deploy-application.sh "<ENV_VALUE>" <DOCKERHUB_USERNAME> <DOCKERHUB_TOKEN> <SERVER_NAME>

set -e

ENV_VALUE="$1"
DOCKERHUB_USERNAME="$2"
DOCKERHUB_TOKEN="$3"
SERVER_NAME="$4"

if [ -z "$ENV_VALUE" ] || [ -z "$DOCKERHUB_USERNAME" ] || [ -z "$DOCKERHUB_TOKEN" ] || [ -z "$SERVER_NAME" ]; then
    echo "사용법: $0 \"<ENV_VALUE>\" <DOCKERHUB_USERNAME> <DOCKERHUB_TOKEN> <SERVER_NAME>"
    exit 1
fi

echo "=== 애플리케이션 배포 시작 ==="

# 헬스체크 스크립트 확인
HEALTH_CHECK_SCRIPT="./scripts/health-check.sh"
if [ ! -f "$HEALTH_CHECK_SCRIPT" ]; then
    echo "헬스체크 스크립트를 찾을 수 없습니다: $HEALTH_CHECK_SCRIPT"
    echo "배포를 위해서는 헬스체크 스크립트가 필요합니다."
    exit 1
fi

# 헬스체크 함수
check_core_services() {
    local timeout=${1:-60}

    echo "헬스체크 실행 중..."
    if $HEALTH_CHECK_SCRIPT all "$timeout"; then
        echo "모든 핵심 서비스가 정상 작동합니다"
        return 0
    else
        echo "일부 서비스에 문제가 있습니다"
        return 1
    fi
}

# .env 파일 작성
echo ".env 파일 생성 중..."
printf "%b\n" "$ENV_VALUE" > .env
echo "DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME" >> .env
echo "SERVER_NAME=$SERVER_NAME" >> .env

# Docker Hub 로그인
echo "Docker Hub 로그인 중..."
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

# 현재 실행 중인 이미지 ID 백업 (롤백용)
CURRENT_IMAGE_ID=""
if docker-compose ps --format "table {{.Image}}" | grep -q "$DOCKERHUB_USERNAME/desserbee"; then
    CURRENT_IMAGE_ID=$(docker-compose ps --format "table {{.Image}}" | grep "$DOCKERHUB_USERNAME/desserbee" | head -1 | awk '{print $2}')
    echo "현재 실행 중인 이미지: $CURRENT_IMAGE_ID"
fi

# Docker 이미지 pull
echo "Docker 이미지 다운로드 중..."
docker pull $DOCKERHUB_USERNAME/desserbee:latest

echo "Redis 상태 확인 중..."
if docker-compose ps redis | grep -q "Up.*healthy"; then
    echo "Redis 정상 - 보존 배포 실행"
    echo "기존 컨테이너 중지 중...(앱과 엔진엑스만)" # 레디스는 인기검색어 유지를 위해 냅둠.
    docker-compose stop app nginx || true
else
    echo "Redis 이상 또는 미실행 - 전체 재시작 실행"
    echo "기존 컨테이너 전체 중지 중..."
    docker-compose down || true
fi

# 새 컨테이너 시작
echo "새 컨테이너 시작 중..."
docker-compose up -d

# 컨테이너 시작 대기 및 헬스체크
echo "컨테이너 시작 대기 중..."
sleep 15

# 배포 성공 여부 확인
echo "배포 상태 확인 중..."
if check_core_services 60; then  # 60초 타임아웃
    echo "배포 성공! 이전 이미지 정리를 진행합니다."

    # 사용하지 않는 이미지 정리 (dangling images)
    echo "사용하지 않는 이미지 정리 중..."
    docker image prune -f

    # 이전 버전 이미지 정리 (현재 latest 제외하고 같은 repository 이미지들)
    echo "이전 버전 이미지 정리 중..."
    OLD_IMAGES=$(docker images $DOCKERHUB_USERNAME/desserbee --format "table {{.ID}} {{.Tag}}" | grep -v "latest" | awk '{print $1}' | head -5)
    if [ -n "$OLD_IMAGES" ]; then
        echo "정리할 이전 이미지들: $OLD_IMAGES"
        echo "$OLD_IMAGES" | xargs -r docker rmi -f || true
    fi

    # 전체 시스템 정리 (사용하지 않는 네트워크, 볼륨 등)
    echo "시스템 정리 중..."
    docker system prune -f --volumes

else
    echo "배포 실패! 롤백을 시도합니다."

    # 롤백 시도
    if [ -n "$CURRENT_IMAGE_ID" ]; then
        echo "이전 이미지로 롤백 중: $CURRENT_IMAGE_ID"
        docker tag $CURRENT_IMAGE_ID $DOCKERHUB_USERNAME/desserbee:rollback
        sed -i "s/:latest/:rollback/g" docker-compose.yml
        docker-compose up -d
        sed -i "s/:rollback/:latest/g" docker-compose.yml  # 원복

        if check_core_services 90; then  # 90초 타임아웃
            echo "롤백 성공"
        else
            echo "롤백도 실패 - 긴급 대응 필요!"

            # 에러 로그 수집
            echo "에러 로그 수집 중..."
            {
                echo "=== 롤백 실패 로그 - $(date) ==="
                echo "서버: $SERVER_NAME"
                echo "Docker 컨테이너 상태:"
                docker-compose ps
                echo "App 로그 (최근 20줄):"
                docker-compose logs --tail=20 app 2>/dev/null || echo "App 로그 수집 실패"
                echo "Nginx 로그 (최근 20줄):"
                docker-compose logs --tail=20 nginx 2>/dev/null || echo "Nginx 로그 수집 실패"
            } | sudo tee /var/log/rollback-failure-$(date +%Y%m%d_%H%M%S).log >/dev/null

            echo "수동 개입이 필요합니다. 로그를 확인하세요."
        fi
    else
        echo "이전 이미지 정보가 없어 롤백할 수 없습니다."
    fi
    exit 1
fi

# 최종 디스크 사용량 확인
echo "=== 정리 후 디스크 사용량 ==="
echo "Docker 이미지 목록:"
docker images $DOCKERHUB_USERNAME/desserbee

echo "Docker 전체 사용량:"
docker system df

# 컨테이너 상태 확인
echo "컨테이너 상태 확인:"
docker-compose ps

echo "=== 애플리케이션 배포 완료 ==="