#!/bin/bash

# 배포 상태 확인 스크립트
# 사용법: ./verify-deployment.sh <SERVER_NAME> <BRANCH_NAME>

set -e

SERVER_NAME="$1"
BRANCH_NAME="$2"

if [ -z "$SERVER_NAME" ] || [ -z "$BRANCH_NAME" ]; then
    echo "사용법: $0 <SERVER_NAME> <BRANCH_NAME>"
    exit 1
fi

echo "========================================"
echo "배포 상태 종합 확인 시작: $SERVER_NAME"
echo "브랜치: $BRANCH_NAME"
echo "========================================"

# SSL 상태 확인
echo "SSL 상태 확인 중..."
./ssl/ssl-health-check.sh $SERVER_NAME || true
echo ""

# 최종 상태 확인
echo "=== 배포 완료 상태 확인 ==="

echo "Docker 컨테이너 상태:"
docker-compose ps
echo ""

echo "cron job 설정 상태:"
crontab -l | grep renew-ssl || echo "cron job 설정 없음"
echo ""

echo "SSL 인증서 상태:"
if sudo ls -la /etc/letsencrypt/live/$SERVER_NAME/ 2>/dev/null; then
    # 인증서 만료일 확인
    expiry_date=$(sudo openssl x509 -enddate -noout -in "/etc/letsencrypt/live/$SERVER_NAME/cert.pem" | cut -d= -f2)
    expiry_epoch=$(date -d "$expiry_date" +%s)
    current_epoch=$(date +%s)
    days_until_expiry=$(( (expiry_epoch - current_epoch) / 86400 ))
    echo "인증서 만료일: $expiry_date"
    echo "남은 일수: $days_until_expiry일"
else
    echo "SSL 인증서 없음"
fi
echo ""

echo "HTTPS 연결 테스트:"
if curl -I https://$SERVER_NAME 2>/dev/null | head -n 1; then
    echo "HTTPS 연결 성공"
else
    echo "HTTPS 연결 실패"
fi
echo ""

echo "로그 파일 상태:"
echo "SSL 갱신 로그: $(ls -la /var/log/ssl-renewal.log 2>/dev/null || echo '없음')"
echo "SSL 상태 로그: $(ls -la /var/log/ssl-health-check.log 2>/dev/null || echo '없음')"
echo ""

echo "========================================"
echo "배포 완료: $(date)"
echo "도메인: https://$SERVER_NAME"
echo "브랜치: $BRANCH_NAME"
echo "========================================"

# 배포 성공 여부 판단
deployment_success=true

# 필수 컨테이너 상태 확인
if [ -f "./scripts/health-check.sh" ]; then
    if ! ./scripts/health-check.sh all 10 >/dev/null 2>&1; then
        echo "일부 서비스가 정상 작동하지 않습니다."
        deployment_success=false
    fi
else
    if ! docker-compose ps | grep -q "Up"; then
        echo "일부 컨테이너가 실행되지 않았습니다."
        deployment_success=false
    fi
fi

# SSL 인증서 확인
if [ ! -d "/etc/letsencrypt/live/$SERVER_NAME" ]; then
    echo "SSL 인증서가 설정되지 않았습니다."
    deployment_success=false
fi

# cron job 확인
if ! crontab -l 2>/dev/null | grep -q "renew-ssl.sh"; then
    echo "SSL 자동 갱신 cron job이 설정되지 않았습니다."
fi

if [ "$deployment_success" = true ]; then
    echo "배포가 성공적으로 완료되었습니다!"
    exit 0
else
    echo "배포 중 일부 문제가 발생했습니다."
    exit 1
fi