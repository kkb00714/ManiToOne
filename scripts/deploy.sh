#!/bin/bash
# AWS SSM에서 JASYPT_KEY 가져오기
echo "## Fetching JASYPT_KEY from AWS SSM..." >> /home/ec2-user/action/spring-deploy.log
JASYPT_KEY=$(aws ssm get-parameter --name "/config/application/JASYPT_KEY" --with-decryption --region ap-northeast-2 --query "Parameter.Value" --output text)

if [ -z "$JASYPT_KEY" ]; then
  echo "## Failed to fetch JASYPT_KEY. Exiting deployment." >> /home/ec2-user/action/spring-deploy.log
  exit 1
fi

echo "## JASYPT_KEY fetched successfully" >> /home/ec2-user/action/spring-deploy.log

BUILD_JAR=$(ls /home/ec2-user/action/build/libs/*SNAPSHOT.jar)
JAR_NAME=$(basename $BUILD_JAR)

echo "## build file name : $JAR_NAME" >> /home/ec2-user/action/spring-deploy.log
echo "## copy build file" >> /home/ec2-user/action/spring-deploy.log

DEPLOY_PATH=/home/ec2-user/action/

cp $BUILD_JAR $DEPLOY_PATH
echo "## current pid" >> /home/ec2-user/action/spring-deploy.log

CURRENT_PID=$(ps -ef | grep "java" | awk 'NR==1 {print $2}')

if [ -z $CURRENT_PID ]
then
  echo "## 현재 구동중인 애플리케이션이 없으므로 종료하지 않습니다." >> /home/ec2-user/action/spring-deploy.log
else
  echo "## kill -15 $CURRENT_PID"
  kill -15 $CURRENT_PID
  sleep 5

# JASYPT_KEY 환경 변수 설정
export JASYPT_ENCRYPTOR_PASSWORD=$JASYPT_KEY
echo "## JASYPT_ENCRYPTOR_PASSWORD exported" >> /home/ec2-user/action/spring-deploy.log

# JAR 파일 실행
DEPLOY_JAR=$DEPLOY_PATH$JAR_NAME
echo "## Deploying JAR file: $DEPLOY_JAR" >> /home/ec2-user/action/spring-deploy.log
nohup java -jar -DJASYPT_ENCRYPTOR_PASSWORD=$JASYPT_ENCRYPTOR_PASSWORD $DEPLOY_JAR >> /home/ec2-user/action/spring-deploy.log 2> /home/ec2-user/action/spring-deploy_err.log &
