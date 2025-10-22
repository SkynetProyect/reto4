NOTAS:
Solo se pueden subir imagenes de tipo .png inferiores a 20MB. imagenes de otro tipo pueden no mostrarse.
El archivo con la version sin minio es leninospina/knote:1.0.0
el dockerhub con la version con minio es leninospina/knote:2.0.0

para testear en local los dockers usados para mongoDB y minio son:


docker run -d \ --name mongo \ -p 27017:27017 \ -e MONGO_INITDB_ROOT_USERNAME=root \ -e MONGO_INITDB_ROOT_PASSWORD=example \ -e MONGO_INITDB_DATABASE=knote \ mongo:latest 

docker run --name=minio --rm -p 9000:9000 -e MINIO_ACCESS_KEY=mykey -e MINIO_SECRET_KEY=mysecret minio/minio server /data

organizado por: Lenin Ospina