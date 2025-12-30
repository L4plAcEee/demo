FROM nginx:alpine

# 复制前端文件到 Nginx 默认目录
COPY . /usr/share/nginx/html/

# 不在这里复制 nginx 配置，而是通过 docker-compose 挂载
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]