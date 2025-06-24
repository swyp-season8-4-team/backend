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
    exit 1
fi

# .env 파일 작성
echo ".env 파일 생성 중..."
printf "%b\n" "$ENV_VALUE" > .env
echo "DOCKERHUB_USERNAME=$DOCKERHUB_USERNAME" >> .env
echo "SERVER_NAME=$SERVER_NAME" >> .env

# Docker Hub 로그인
echo "Docker Hub 로그인 중..."
echo "$DOCKERHUB_TOKEN" | docker login -u "$DOCKERHUB_USERNAME" --password-stdin

# Docker 이미지 pull
echo "Docker 이미지 다운로드 중..."
docker pull "$DOCKERHUB_USERNAME/desserbee:latest"

# 기존 컨테이너 중지 및 제거 (Redis는 보존)
echo "기존 컨테이너 중지 및 제거 중..."
docker-compose stop app nginx || true
docker-compose rm -f app nginx || true

# Docker 리소스 정리
echo "Docker 리소스 정리 중..."
docker system prune -f --volumes
docker image prune -f

# 새 컨테이너 시작
echo "새 컨테이너 시작 중..."
docker-compose up -d

# 컨테이너 시작 대기
echo "컨테이너 시작 대기 중..."
sleep 30

# 배포 상태 확인
echo "배포 상태 확인 중..."
if $HEALTH_CHECK_SCRIPT all 60; then
    echo "✅ 배포 성공!"
    
    # Docker 이미지 정리
    echo "Docker 이미지 정리 중..."
    docker image prune -f
    docker system prune -f --volumes
    
    # 최종 상태 확인
    echo "=== 최종 상태 확인 ==="
    docker-compose ps
    docker system df
    
    echo "=== 애플리케이션 배포 완료 ==="
else
    echo "❌ 배포 실패!"
    echo "컨테이너 로그 확인:"
    docker-compose logs --tail=20 app
    exit 1
fi