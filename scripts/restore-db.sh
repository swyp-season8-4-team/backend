#!/bin/bash

echo "Starting DB restoration..."

cd /home/ec2-user/app

# .env 파일에서 MySQL 비밀번호만 추출
MYSQL_ROOT_PASSWORD=$(grep "^MYSQL_ROOT_PASSWORD=" .env | cut -d '=' -f2)

# 1. MySQL 컨테이너 시작 대기
echo "Waiting for MySQL container to be ready..."
until docker exec desserbee-mysql mysqladmin ping -h localhost -uroot -p"$MYSQL_ROOT_PASSWORD" --silent 2>/dev/null; do
    echo "MySQL is unavailable - sleeping"
    sleep 3
done

echo "MySQL is ready!"

# 2. 백업 파일 복원
echo "Restoring database from backup..."
docker exec -i desserbee-mysql sh -c "mysql -uroot -p'$MYSQL_ROOT_PASSWORD' dessertbee_prod" < db_backup_2025-12-17.sql 2>/dev/null

if [ $? -eq 0 ]; then
    echo "Database restored successfully!"
else
    echo "Database restoration failed!"
    exit 1
fi

# 3. 테이블 확인
echo "Verifying tables..."
docker exec desserbee-mysql mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "USE dessertbee_prod; SHOW TABLES;" 2>/dev/null

echo "Restoration complete!"