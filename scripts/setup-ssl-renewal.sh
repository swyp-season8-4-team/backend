#!/bin/bash

# SSL 자동 갱신 설정 스크립트
set -e

echo "=== SSL 자동 갱신 설정 시작 ==="

# SSL 자동 갱신 cron job 설정
echo "SSL 자동 갱신 cron job 설정 중..."
./ssl/renew-ssl.sh setup-cron

# cron job 설정 확인 및 강제 등록 (혹시 실패했을 경우)
if ! crontab -l 2>/dev/null | grep -q "renew-ssl.sh"; then
  echo "cron job 강제 등록 중..."
  (crontab -l 2>/dev/null; echo "0 6,18 * * * /home/ec2-user/app/ssl/renew-ssl.sh >> /var/log/ssl-renewal.log 2>&1") | crontab -
  echo "cron job 강제 등록 완료"
else
  echo "cron job 설정 확인됨"
fi

# cron 서비스 상태 확인
echo "cron 서비스 상태 확인:"
sudo systemctl status crond --no-pager || true

# 설정된 cron job 표시
echo "현재 설정된 SSL 갱신 cron job:"
crontab -l | grep renew-ssl || echo "cron job 설정 없음"

echo "=== SSL 자동 갱신 설정 완료 ==="