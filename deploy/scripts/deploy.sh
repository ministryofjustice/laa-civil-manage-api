#!/bin/bash

ENVIRONMENT=$1
# Convert the branch name into a string that can be turned into a valid URL

deploy_branch() {
# Set the ingress name, needs release name, namespace and -green suffix
  RELEASE_NAME=$(echo "$branch_name" | tr '[:upper:]' '[:lower:]' | sed 's:^\w*\/::' | tr -s ' _/[]().' '-' | cut -c1-18 | sed 's/-$//')
  IDENTIFIER="$RELEASE_NAME-laa-civil-manage-api-$K8S_NAMESPACE-green"
  RELEASE_HOST="$RELEASE_NAME-laa-civil-manage-api-$ENVIRONMENT.cloud-platform.service.justice.gov.uk"
  echo "github ref: $branch_name; release name: $RELEASE_NAME; identifier: $IDENTIFIER; release host: $RELEASE_HOST"
  echo "Deploying commit: $GITHUB_SHA under release name: '$RELEASE_NAME'..."
  echo "This is a branch deployment"

  helm upgrade "$RELEASE_NAME" ./deploy/infrastructure/helm/. \
                --install --wait --timeout 10m \
                --namespace="${K8S_NAMESPACE}" \
                --values ./deploy/infrastructure/helm/values/"$ENVIRONMENT".yaml \
                --set image.repository="$REGISTRY/$REPOSITORY" \
                --set image.tag="$IMAGE_TAG" \
                --set mockServer.image.repository="$REGISTRY/$REPOSITORY" \
                --set mockServer.image.tag="fake-data-store-$IMAGE_TAG" \
                --set ingress.annotations."external-dns\.alpha\.kubernetes\.io/set-identifier"="$IDENTIFIER" \
                --set ingress.hosts[0].host="$RELEASE_HOST"
}

deploy_main() {
  RELEASE_HOST="laa-civil-manage-api-$ENVIRONMENT.cloud-platform.service.justice.gov.uk"
  helm upgrade laa-civil-manage-api ./deploy/infrastructure/helm/. \
                --install --wait --timeout 10m \
                --namespace="${K8S_NAMESPACE}" \
                --values ./deploy/infrastructure/helm/values/"$ENVIRONMENT".yaml \
                --set image.repository="$REGISTRY/$REPOSITORY" \
                --set image.tag="$IMAGE_TAG" \
                --set mockServer.image.repository="$REGISTRY/$REPOSITORY" \
                --set mockServer.image.tag="fake-data-store-$IMAGE_TAG"
}



branch_name="$GITHUB_HEAD_REF" # Branch name if this is a pull-request event
if [ -z "$branch_name" ]; then
  branch_name="$GITHUB_REF_NAME" # Branch name if this is a push event
fi


if [[ ("$ENVIRONMENT" == 'dev' || "$ENVIRONMENT" == 'staging' || "$ENVIRONMENT" == 'production') && "$branch_name" == "main" ]]
then
  echo "Deploying Main"
  deploy_main
else
  if deploy_branch; then
    echo "Deploy succeeded"
  else
    echo "Deploy failed. Attempting rollback"
    if helm rollback "$RELEASE_NAME"; then
      echo "Rollback succeeded. Retrying deploy"
      deploy_branch
    else
      echo "Rollback failed. Consider manually running 'helm delete $RELEASE_NAME'"
      exit 1
    fi
  fi
fi
