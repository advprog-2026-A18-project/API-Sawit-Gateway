# Cara Menjalankan
-  mkdir <parent_folder>
-  cd <parent_folder>
-  git clone <URL_REPO_Services> <nama_folder_services> (lakukan sebanyak n services)
-  git clone <URL_REPO_gateway> <nama_folder_gateway>
-  cd <nama_folder_services> && ./gradlew clean build -x test && cd ../ (lakukan sebanyak n services)
-  cd <nama_folder_gateway> && ./gradlew clean build -x test
-  docker-compose up --build
-  Service akan berhasil berjalan sesuai port yang terdaftar
