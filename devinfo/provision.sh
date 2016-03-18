#!/usr/bin/env bash
export DB_USER=helin

# update before the show begins
apt-get update

# Some basic tools
apt-get install -y vim 

# RabbitMQ

# Add RabbitMQ to source list
echo "deb http://www.rabbitmq.com/debian/ testing main" >> /etc/apt/sources.list
curl http://www.rabbitmq.com/rabbitmq-signing-key-public.asc | sudo apt-key add -

# update APT
apt-get update
# Install RabbitMQ
apt-get install rabbitmq-server -y

# Enable Management Console:
# Connect to port 15672 and you'll be provided with an UI to manager RabbitMQ
rabbitmq-plugins enable rabbitmq_management

# Add new user 'admin' with password 'helin'
sudo rabbitmqctl add_user admin helin
sudo rabbitmqctl set_user_tags admin administrator
sudo rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"

# Install PostgreSQL
sudo apt-get install -y postgresql postgresql-contrib

# create new user and DB
sudo -u postgres createdb $DB_USER 
sudo -u postgres createuser $DB_USER -s       # -s for superuser
# change password
sudo -u postgres psql -c "alter user $DB_USER with password '$DB_USER';"

# We need to alter vagrant configuration, so that we can connect from
# outside of the virtual machine to the db
# From: http://jamie.ideasasylum.com/2012/09/connecting-navicat-to-postgresql-on-vagrant/
# Another source: http://www.bentedder.com/use-pgadmin-access-postgres-database-within-vagrant-box/

sudo sh -c  "echo 'host all all all password' >> /etc/postgresql/9.3/main/pg_hba.conf"
sudo sh -c  "echo listen_addresses = \'*\' >> /etc/postgresql/9.3/main/postgresql.conf"

# restart postgres so that all changes get activated
sudo /etc/init.d/postgresql restart