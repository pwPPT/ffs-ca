Aby uruchomić projekt:

1. uruchamiamy postgreSQL: 
    docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name ffs_ca_db -e POSTGRES_USER=admin -e POSTGRES_PASSWORD=admin -e POSTGRES_DB=ffs_ca -p 5432:5432 postgres:10.5
2. Tworzymy schemat o nazwie 'ffs_ca'
3. Tworzymy wymagane tabele: definicje znajdują się w pliku src/main/resources/db/migration/R__initialize.sql
4. Uruchamiamy aplikację poleceniem: mvn quarkus:dev