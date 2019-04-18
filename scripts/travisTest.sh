#!/bin/bash
set -euxo pipefail

##############################################################################
##
##  Travis CI test script
##
##############################################################################

mvn -q package

kubectl apply -f kubernetes.yaml

sleep 120

kubectl get pods

echo `minikube ip`

mvn verify -Ddockerfile.skip=true -Dcluster.ip=`minikube ip`

kubectl logs $(kubectl get pods -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}' | grep system | head -1)

kubectl logs $(kubectl get pods -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}' | grep system | tail -1)

kubectl logs $(kubectl get pods -o jsonpath='{range .items[*]}{.metadata.name}{"\n"}' | grep inventory)
