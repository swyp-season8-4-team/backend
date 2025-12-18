#!/bin/bash

# SSL 인증서 자동 갱신 스크립트 (호스트 기반)
# 사용법:
#   ./renew-ssl.sh                 # 갱신 실행
#   ./renew-ssl.sh setup-cron      # cron job 설정
#   ./renew-ssl.sh check           # 인증서 상태 확인

set -e

ACTION=${1:-renew}
LOG_FILE="/var/log/ssl-renewal.log"
COMPOSE_DIR="/home/ec2-user/app"

# 로그 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 인증서 갱신 함수
renew_certificates() {
    log "=== SSL 인증서 갱신 시작 ==="

    # Nginx 중지
    cd "$COMPOSE_DIR"
    docker-compose stop nginx
    log "Nginx 중지 완료"

    # standalone 방식으로 갱신
    if sudo certbot renew --standalone --quiet; then
        log "인증서 갱신 성공"

        # 심볼릭 링크 재생성
        sudo ln -sf /etc/letsencrypt/live/desserbee.com /etc/letsencrypt/live/api.desserbee.com
        log "심볼릭 링크 생성 완료"

        RENEWAL_NEEDED=true
    else
        log "인증서 갱신 실패"
    fi

    # Nginx 재시작
    docker-compose start nginx
    log "Nginx 재시작 완료"

    log "SSL 인증서 갱신 프로세스 완료"
}

# 인증서 상태 확인 함수
check_certificates() {
    log "=== SSL 인증서 상태 확인 ==="

    if command -v certbot &> /dev/null; then
        log "현재 설치된 인증서 목록:"
        sudo certbot certificates

        log "갱신 예정 확인 (dry-run):"
        sudo certbot renew --dry-run

        log "만료일 확인:"
        for cert_dir in /etc/letsencrypt/live/*/; do
            if [ -d "$cert_dir" ]; then
                domain=$(basename "$cert_dir")
                if [ -f "$cert_dir/cert.pem" ]; then
                    expiry=$(sudo openssl x509 -enddate -noout -in "$cert_dir/cert.pem" | cut -d= -f2)
                    log "도메인 $domain 만료일: $expiry"
                fi
            fi
        done
    else
        log "certbot이 설치되어 있지 않습니다."
        exit 1
    fi
}

# cron job 설정 함수
setup_cron() {
    log "=== SSL 갱신 cron job 설정 ==="

    # 매일 오전 3시와 오후 3시에 2번 체크 (Let's Encrypt 권장사항)
    CRON_JOB="0 3,15 * * * /home/ec2-user/app/ssl/renew-ssl.sh >> /var/log/ssl-renewal.log 2>&1"

    # 기존 cron job 확인
    if crontab -l 2>/dev/null | grep -q "renew-ssl.sh"; then
        log "SSL 갱신 cron job이 이미 설정되어 있습니다."
        log "기존 cron jobs:"
        crontab -l | grep "renew-ssl.sh" || true

        # 기존 것을 제거하고 새로 설정할지 묻기
        log "기존 설정을 업데이트합니다..."
        crontab -l 2>/dev/null | grep -v "renew-ssl.sh" | crontab -
    fi

    # 새로운 cron job 추가
    (crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -
    log "SSL 갱신 cron job이 설정되었습니다."
    log "매일 오전 3시와 오후 3시에 인증서 갱신을 확인합니다."

    # 현재 cron jobs 표시
    log "현재 설정된 cron jobs:"
    crontab -l || log "설정된 cron job이 없습니다."

    # 로그 파일 권한 설정
    sudo touch "$LOG_FILE"
    sudo chown ec2-user:ec2-user "$LOG_FILE"
    sudo chmod 644 "$LOG_FILE"

    # 로그 로테이션 설정
    setup_log_rotation
}

# 로그 로테이션 설정 함수
setup_log_rotation() {
    log "로그 로테이션 설정 중..."

    sudo tee /etc/logrotate.d/ssl-renewal > /dev/null <<EOF
/var/log/ssl-renewal.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    copytruncate
    notifempty
}
EOF

    log "로그 로테이션 설정 완료 (30일 보관)"
}

# 메인 실행부
case "$ACTION" in
    "renew")
        renew_certificates "$@"
        ;;
    "check")
        check_certificates
        ;;
    "setup-cron")
        setup_cron
        ;;
    *)
        echo "사용법: $0 [renew|check|setup-cron]"
        echo ""
        echo "옵션:"
        echo "  renew      - SSL 인증서 갱신 실행 (기본값)"
        echo "  check      - 인증서 상태 확인"
        echo "  setup-cron - 자동 갱신 cron job 설정"
        echo ""
        echo "추가 옵션:"
        echo "  renew --force-restart - 갱신 여부와 상관없이 nginx 재시작"
        exit 1
        ;;
esac