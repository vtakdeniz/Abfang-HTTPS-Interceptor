openssl genrsa -out www.youtube.com.key 2048

openssl req -new -sha256 -key SERVER.key -subj "/C=US/ST=North Carolina/O=ORG/OU=ORG_UNIT/CN=www.youtube.com" -reqexts SAN -config <(cat /etc/ssl/openssl.cnf <(printf "\n[SAN]\nsubjectAltName=DNS:www.youtube.com")) -out www.youtube.com.crt

openssl x509 -req -extfile <(printf "subjectAltName=DNS:www.youtube.com") -days 120 -in www.youtube.com.crt -CA root.crt -CAkey root.key -CAcreateserial -outwww.youtube.com.crt -sha256



-------------------


openssl pkcs12 -export -in www.youtube.com.crt -inkey www.youtube.com.key -out youtube.p12 -name youtube -CAfile root.crt -caname interceptor

keytool -importkeystore -deststorepass qwerty -destkeypass qwerty -destkeystore youtube.keystore -srckeystore youtube.p12 -srcstoretype PKCS12 -srcstorepass qwerty -alias youtube

