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

    # certbot 설치 확인
    if ! command -v certbot &> /dev/null; then
        log "certbot이 설치되어 있지 않습니다."
        return 1
    fi

    # nginx 컨테이너 상태 확인
    cd "$COMPOSE_DIR"
    if ! docker-compose ps nginx | grep -q "Up"; then
        log "nginx 컨테이너가 실행 중이지 않습니다. nginx를 시작합니다."
        docker-compose up -d nginx
        sleep 10
    fi

    # webroot 디렉토리 확인 및 생성
    WEBROOT_DIR="/var/www/certbot"
    if [ ! -d "$WEBROOT_DIR" ]; then
        log "webroot 디렉토리를 생성합니다: $WEBROOT_DIR"
        sudo mkdir -p "$WEBROOT_DIR"
        sudo chown -R ec2-user:ec2-user "$WEBROOT_DIR"
    fi

    # 갱신 전 인증서 상태 확인
    log "갱신 전 인증서 상태 확인:"
    sudo certbot certificates

    # 실제 갱신 실행 (30일 이내 만료 시에만 갱신됨)
    log "certbot으로 인증서 갱신 확인 중..."
    
    # 갱신 전 로그 백업
    sudo cp /var/log/letsencrypt/letsencrypt.log /var/log/letsencrypt/letsencrypt.log.backup 2>/dev/null || true
    
    if sudo certbot renew --webroot --webroot-path="$WEBROOT_DIR" --quiet --no-self-upgrade --agree-tos; then
        log "인증서 갱신 확인 완료"

        # 갱신 여부 확인 (로그 파일 비교)
        if sudo diff /var/log/letsencrypt/letsencrypt.log.backup /var/log/letsencrypt/letsencrypt.log 2>/dev/null | grep -q "renewed"; then
            log "새로운 인증서가 발급되었습니다"
            RENEWAL_NEEDED=true
        else
            log "갱신이 필요한 인증서가 없습니다"
            RENEWAL_NEEDED=false
        fi
    else
        log "인증서 갱신 실패"
        return 1
    fi

    # nginx 재시작 (새로운 인증서가 있는 경우 또는 강제 재시작)
    if [ "$RENEWAL_NEEDED" = true ] || [ "$2" = "--force-restart" ]; then
        log "nginx 컨테이너 재시작 중..."

        # nginx 설정 테스트
        if docker-compose exec nginx nginx -t 2>/dev/null; then
            log "nginx 설정이 유효합니다"
        else
            log "nginx 설정에 문제가 있습니다. 설정을 확인하세요."
            return 1
        fi

        # nginx만 재시작 (다른 서비스에 영향 없음)
        if docker-compose restart nginx; then
            log "nginx 재시작 완료 - 새 인증서가 적용되었습니다"
            
            # 재시작 후 헬스체크
            sleep 5
            if docker-compose ps nginx | grep -q "Up"; then
                log "nginx가 정상적으로 재시작되었습니다"
            else
                log "nginx 재시작 후 상태가 비정상입니다"
                return 1
            fi
        else
            log "nginx 재시작 실패"
            return 1
        fi
    else
        log "인증서 갱신이 없어 nginx 재시작을 건너뜁니다"
    fi

    log "SSL 인증서 갱신 프로세스 완료"
}

# 인증서 상태 확인 함수
check_certificates() {
    log "=== SSL 인증서 상태 확인 ==="

    if command -v certbot &> /dev/null; then
        log "현재 설치된 인증서 목록:"
        sudo certbot certificates

        log "갱신 예정 확인 (dry-run):"
        if sudo certbot renew --dry-run --webroot --webroot-path="/var/www/certbot" --quiet; then
            log "dry-run 성공 - 갱신 프로세스가 정상입니다"
        else
            log "dry-run 실패 - 갱신 프로세스에 문제가 있습니다"
        fi

        log "만료일 확인:"
        for cert_dir in /etc/letsencrypt/live/*/; do
            if [ -d "$cert_dir" ]; then
                domain=$(basename "$cert_dir")
                if [ -f "$cert_dir/cert.pem" ]; then
                    expiry=$(sudo openssl x509 -enddate -noout -in "$cert_dir/cert.pem" | cut -d= -f2)
                    log "도메인 $domain 만료일: $expiry"
                    
                    # 만료일까지 남은 일수 계산
                    expiry_epoch=$(date -d "$expiry" +%s 2>/dev/null || echo "0")
                    current_epoch=$(date +%s)
                    days_left=$(( (expiry_epoch - current_epoch) / 86400 ))
                    
                    if [ $days_left -gt 0 ]; then
                        log "  남은 일수: $days_left일"
                        if [ $days_left -lt 30 ]; then
                            log "  ⚠️ 30일 이내 만료 - 갱신이 필요합니다"
                        fi
                    else
                        log "  ⚠️ 인증서가 만료되었습니다!"
                    fi
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

        # 기존 것을 제거하고 새로 설정
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