#!/bin/bash
cd ..
az acr login --name tbsacr
docker build . -t tbsacr.azurecr.io/pagesuccess:1.0.0
docker push tbsacr.azurecr.io/pagesuccess:1.0.0

