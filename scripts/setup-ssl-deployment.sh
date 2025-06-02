#!/bin/bash

# SSL 인증서 배포 설정 스크립트
# 사용법: ./setup-ssl-deployment.sh <도메인명>

set -e

DOMAIN=$1

if [ -z "$DOMAIN" ]; then
    echo "사용법: $0 <도메인명>"
    exit 1
fi

echo "=== SSL 인증서 배포 설정 시작 ==="
echo "도메인: $DOMAIN"

# SSL 스크립트 실행 권한 부여
chmod +x ssl/setup-ssl.sh ssl/renew-ssl.sh ssl/ssl-health-check.sh

# SSL 인증서 존재 여부에 따른 분기 처리
if [ ! -d "/etc/letsencrypt/live/$DOMAIN" ]; then
  echo "=== 첫 번째 배포: SSL 인증서 초기 설정 ==="

  # HTTP 전용 임시 nginx 설정 생성
  echo "HTTP 전용 nginx 임시 시작..."
  cat nginx/conf/default-http-only.conf.template | sed "s/\${SERVER_NAME}/$DOMAIN/g" > nginx/conf/default-http-only.conf

  # 임시 nginx 설정으로 시작
  docker run -d --name temp-nginx \
    -p 80:80 \
    -v $(pwd)/nginx/conf/default-http-only.conf:/etc/nginx/conf.d/default.conf \
    -v /var/www/certbot:/var/www/certbot:ro \
    nginx:latest

  # nginx 시작 대기
  echo "nginx 시작 대기 중..."
  sleep 10

  echo "SSL 인증서 발급 중..."
  ./ssl/setup-ssl.sh $DOMAIN

  # 임시 nginx 컨테이너 정리
  docker stop temp-nginx || true
  docker rm temp-nginx || true

  echo "SSL 인증서 초기 설정 완료"
else
  echo "=== 기존 SSL 인증서 발견: 정상 배포 진행 ==="
fi

# Nginx 설정 파일 생성 (템플릿에서 서버 이름 변수 치환)
echo "Nginx 설정 파일 생성 중..."
mkdir -p nginx/conf
cat nginx/conf/default.conf.template | sed "s/\${SERVER_NAME}/$DOMAIN/g" > nginx/conf/default.conf

echo "=== SSL 배포 설정 완료 ==="