#!/bin/bash

# SSL 초기 설정 스크립트
# 사용법: ./setup-ssl.sh <도메인명>

set -e

DOMAIN=$1

echo "=== SSL 인증서 초기 설정 시작 ==="
echo "도메인: $DOMAIN"

# 도메인 유효성 간단 확인 (선택적)
if [ -z "$DOMAIN" ] || [[ ! "$DOMAIN" =~ ^[a-zA-Z0-9.-]+$ ]]; then
    echo "유효하지 않은 도메인: $DOMAIN"
    exit 1
fi

# 필요한 디렉토리 생성
sudo mkdir -p /var/www/certbot
sudo mkdir -p /etc/letsencrypt
sudo mkdir -p /var/lib/letsencrypt

# certbot - 깃허브 액션 워크 플로우에서 설치 명령어 삽입
if ! command -v certbot &> /dev/null; then
    echo "certbot이 설치되어 있지 않습니다. CI/CD 스크립트를 확인하세요."
    exit 1
fi

# webroot - 깃허브 액션 워크 플로우에서 디렉토리 권한 설정
if [ ! -d "/var/www/certbot" ]; then
    echo "/var/www/certbot 디렉토리가 없습니다. CI/CD 스크립트를 확인하세요."
    exit 1
fi

# HTTP 서버 접근 가능 여부 확인 (Let's Encrypt 검증 준비)
echo "HTTP 서버 접근성 확인 중..."
for i in {1..6}; do
    if curl -sSf --connect-timeout 5 "http://$DOMAIN/.well-known/acme-challenge/" > /dev/null 2>&1; then
        echo "HTTP 서버가 준비되었습니다."
        break
    else
        echo "HTTP 서버 준비 대기 중... ($i/6)"
        sleep 10
    fi
    if [ $i -eq 6 ]; then
        echo "⚠️  HTTP 서버 준비 확인에 실패했습니다. 인증서 발급을 시도합니다..."
    fi
done

echo "Let's Encrypt 인증서 발급 중..."

# 초기 인증서 발급 (webroot 방식)
sudo certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email admin@desserbee.com \
    --agree-tos \
    --no-eff-email \
    --keep-until-expiring \
    --non-interactive \
    -d $DOMAIN

if [ $? -eq 0 ]; then
    echo "SSL 인증서가 성공적으로 발급되었습니다!"
    echo "인증서 경로: /etc/letsencrypt/live/$DOMAIN/"

    # 인증서 파일 권한 설정
    sudo chmod -R 755 /etc/letsencrypt

    echo "인증서 만료일 확인:"
    sudo certbot certificates -d $DOMAIN
else
    echo "SSL 인증서 발급에 실패했습니다."
    echo "다음을 확인해주세요:"
    echo "1. 도메인 DNS가 올바르게 설정되어 있는지"
    echo "2. 80번 포트가 열려있고 nginx가 실행 중인지"
    echo "3. /.well-known/acme-challenge/ 경로가 접근 가능한지"
    exit 1
fi

echo "=== SSL 인증서 초기 설정 완료 ==="