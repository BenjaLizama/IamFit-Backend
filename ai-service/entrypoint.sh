#!/bin/sh

# Decodifica credenciales GCP
if [ -n "$GCP_CREDENTIALS" ]; then
    echo "$GCP_CREDENTIALS" | base64 -d > /tmp/gcp-credentials.json
    export GOOGLE_APPLICATION_CREDENTIALS=/tmp/gcp-credentials.json
    echo ">>> GCP credentials cargadas correctamente"
else
    echo ">>> GCP_CREDENTIALS no encontrada"
fi

# Escribe la clave pública RSA en un archivo temporal
if [ -n "$RSA_PUBLIC" ]; then
    echo "-----BEGIN PUBLIC KEY-----" > /tmp/auth-public.pem
    echo "$RSA_PUBLIC" | fold -w 64 >> /tmp/auth-public.pem
    echo "-----END PUBLIC KEY-----" >> /tmp/auth-public.pem
    echo ">>> Clave pública RSA escrita correctamente"
else
    echo ">>> RSA_PUBLIC no encontrada"
fi

exec java -Dspring.profiles.active=prod \
     -Dspring.security.oauth2.resourceserver.jwt.public-key-location=file:/tmp/auth-public.pem \
     -jar /app/app.jar