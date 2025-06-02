#!/bin/bash

# SSL 인증서 상태 모니터링 스크립트
DOMAIN=${1:-$(hostname -f)}
LOG_FILE="/var/log/ssl-health-check.log"

# 로그 함수
log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 색상 출력 함수
print_status() {
    local status=$1
    local message=$2
    case $status in
        "OK")
            echo -e "\033[32m  $message\033[0m"
            ;;
        "WARNING")
            echo -e "\033[33m  $message\033[0m"
            ;;
        "ERROR")
            echo -e "\033[31m $message\033[0m"
            ;;
        "INFO")
            echo -e "\033[36m  $message\033[0m"
            ;;
    esac
}

# 인증서 만료일 확인
check_cert_expiry() {
    log "=== SSL 인증서 만료일 확인: $DOMAIN ==="

    if [ ! -f "/etc/letsencrypt/live/$DOMAIN/cert.pem" ]; then
        print_status "ERROR" "인증서 파일을 찾을 수 없습니다: /etc/letsencrypt/live/$DOMAIN/cert.pem"
        return 1
    fi

    # 인증서 만료일 추출
    expiry_date=$(sudo openssl x509 -enddate -noout -in "/etc/letsencrypt/live/$DOMAIN/cert.pem" | cut -d= -f2)
    expiry_epoch=$(date -d "$expiry_date" +%s)
    current_epoch=$(date +%s)
    days_until_expiry=$(( (expiry_epoch - current_epoch) / 86400 ))

    print_status "INFO" "인증서 만료일: $expiry_date"
    print_status "INFO" "남은 일수: $days_until_expiry일"

    if [ $days_until_expiry -le 7 ]; then
        print_status "ERROR" "인증서가 7일 이내에 만료됩니다! 즉시 갱신이 필요합니다."
        return 2
    elif [ $days_until_expiry -le 30 ]; then
        print_status "WARNING" "인증서가 30일 이내에 만료됩니다. 갱신을 확인하세요."
        return 1
    else
        print_status "OK" "인증서 상태가 양호합니다."
        return 0
    fi
}

# 웹서버 SSL 연결 테스트
check_ssl_connection() {
    log "=== SSL 연결 테스트: https://$DOMAIN ==="

    # curl로 SSL 연결 테스트
    if curl -sSf --connect-timeout 10 "https://$DOMAIN" > /dev/null 2>&1; then
        print_status "OK" "HTTPS 연결이 정상적으로 작동합니다."
    else
        print_status "ERROR" "HTTPS 연결에 실패했습니다."
        return 1
    fi

    # openssl로 상세 SSL 정보 확인
    ssl_info=$(echo | timeout 10 openssl s_client -connect "$DOMAIN:443" -servername "$DOMAIN" 2>/dev/null | openssl x509 -noout -subject -issuer 2>/dev/null)

    if [ $? -eq 0 ] && [ -n "$ssl_info" ]; then
        print_status "OK" "SSL 인증서 정보:"
        echo "$ssl_info" | while read line; do
            print_status "INFO" "  $line"
        done
    else
        print_status "WARNING" "SSL 연결 상세 정보를 가져올 수 없습니다."
    fi
}

# certbot 상태 확인
check_certbot_status() {
    log "=== certbot 상태 확인 ==="

    if ! command -v certbot &> /dev/null; then
        print_status "ERROR" "certbot이 설치되어 있지 않습니다."
        return 1
    fi

    print_status "OK" "certbot이 설치되어 있습니다."

    # certbot 인증서 목록 확인
    cert_list=$(sudo certbot certificates 2>/dev/null | grep "Certificate Name" | wc -l)
    print_status "INFO" "관리 중인 인증서 수: $cert_list개"

    # 갱신 가능 여부 확인 (dry-run)
    if sudo certbot renew --dry-run --quiet 2>/dev/null; then
        print_status "OK" "인증서 갱신 테스트가 성공했습니다."
    else
        print_status "ERROR" "인증서 갱신 테스트가 실패했습니다."
        return 1
    fi
}

# nginx 컨테이너 상태 확인
check_nginx_status() {
    log "=== nginx 컨테이너 상태 확인 ==="

    if docker-compose -f /home/ec2-user/app/docker-compose.yml ps nginx | grep -q "Up"; then
        print_status "OK" "nginx 컨테이너가 정상적으로 실행 중입니다."

        # nginx 설정 테스트
        if docker exec desserbee-nginx nginx -t 2>/dev/null; then
            print_status "OK" "nginx 설정이 유효합니다."
        else
            print_status "ERROR" "nginx 설정에 오류가 있습니다."
            return 1
        fi
    else
        print_status "ERROR" "nginx 컨테이너가 실행되지 않았습니다."
        return 1
    fi
}

# cron job 상태 확인
check_cron_status() {
    log "=== SSL 갱신 cron job 확인 ==="

    cron_count=$(crontab -l 2>/dev/null | grep -c "renew-ssl.sh")

    if [ $cron_count -gt 0 ]; then
        print_status "OK" "SSL 갱신 cron job이 설정되어 있습니다."
        print_status "INFO" "설정된 cron job:"
        crontab -l | grep "renew-ssl.sh" | while read line; do
            print_status "INFO" "  $line"
        done
    else
        print_status "WARNING" "SSL 갱신 cron job이 설정되어 있지 않습니다."
        return 1
    fi
}

# 로그 파일 확인
check_renewal_logs() {
    log "=== 최근 갱신 로그 확인 ==="

    if [ -f "/var/log/ssl-renewal.log" ]; then
        last_renewal=$(tail -n 20 "/var/log/ssl-renewal.log" | grep "갱신 프로세스 완료" | tail -n 1)
        if [ -n "$last_renewal" ]; then
            print_status "OK" "마지막 갱신 확인: $last_renewal"
        else
            print_status "INFO" "최근 갱신 기록이 없습니다."
        fi
    else
        print_status "WARNING" "갱신 로그 파일이 없습니다: /var/log/ssl-renewal.log"
    fi

    # Let's Encrypt 로그 확인
    if [ -f "/var/log/letsencrypt/letsencrypt.log" ]; then
        recent_errors=$(sudo tail -n 50 "/var/log/letsencrypt/letsencrypt.log" | grep -i "error\|failed" | tail -n 3)
        if [ -n "$recent_errors" ]; then
            print_status "WARNING" "최근 Let's Encrypt 오류:"
            echo "$recent_errors" | while read line; do
                print_status "WARNING" "  $line"
            done
        fi
    fi
}

# 메인 실행부
main() {
    echo "========================================"
    echo "SSL 상태 종합 점검 시작: $DOMAIN"
    echo "========================================"

    exit_code=0

    check_cert_expiry || exit_code=$?
    echo

    check_ssl_connection || exit_code=$?
    echo

    check_certbot_status || exit_code=$?
    echo

    check_nginx_status || exit_code=$?
    echo

    check_cron_status || exit_code=$?
    echo

    check_renewal_logs
    echo

    echo "========================================"
    if [ $exit_code -eq 0 ]; then
        print_status "OK" "모든 SSL 상태 점검이 완료되었습니다."
    elif [ $exit_code -eq 1 ]; then
        print_status "WARNING" "일부 항목에 주의가 필요합니다."
    else
        print_status "ERROR" "긴급한 조치가 필요한 문제가 발견되었습니다."
    fi
    echo "========================================"

    log "SSL 상태 점검 완료 (종료 코드: $exit_code)"
    return $exit_code
}

# 스크립트 실행
main "$@"