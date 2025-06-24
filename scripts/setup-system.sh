#!/bin/bash

# 시스템 환경 설정 스크립트
set -e

echo "=== 시스템 환경 설정 시작 ==="

# Docker 설치 (Amazon Linux 2023 방식)
if ! command -v docker &> /dev/null; then
  echo "Installing Docker..."
  sudo dnf update -y
  sudo dnf install docker -y
  sudo systemctl start docker
  sudo systemctl enable docker
  sudo usermod -aG docker ec2-user
  # 그룹 적용
  newgrp docker || true
  echo "Docker 설치 완료"
else
  echo "Docker 이미 설치됨"
fi

# Docker Compose 설치
if ! command -v docker-compose &> /dev/null; then
  echo "Installing Docker Compose..."
  sudo curl -L "https://github.com/docker/compose/releases/download/v2.18.1/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
  sudo chmod +x /usr/local/bin/docker-compose
  sudo ln -s /usr/local/bin/docker-compose /usr/bin/docker-compose
  echo "Docker Compose 설치 완료"
else
  echo "Docker Compose 이미 설치됨"
fi

# cron 설치 및 시작 (SSL 자동 갱신용)
if ! command -v crontab &> /dev/null; then
  echo "Installing cronie..."
  sudo dnf install -y cronie
  sudo systemctl start crond
  sudo systemctl enable crond
  echo "Cron 설치 및 시작 완료"
else
  echo "Cron 이미 설치됨"
  sudo systemctl start crond || true
  sudo systemctl enable crond || true
fi

# certbot 설치 (Amazon Linux 2023)
if ! command -v certbot &> /dev/null; then
  echo "Installing certbot..."
  sudo dnf install -y python3-pip
  sudo pip3 install certbot
  echo "Certbot 설치 완료"
else
  echo "Certbot 이미 설치됨"
fi

# SSL 디렉토리 생성 및 권한 설정
echo "SSL 디렉토리 설정 중..."
sudo mkdir -p /var/www/certbot
sudo chmod 755 /var/www/certbot
sudo chown -R ec2-user:ec2-user /var/www/certbot

echo "=== 시스템 환경 설정 완료 ==="