# Alert Testing Guide

This document outlines how to test the Prometheus alerts using the non-prod `laa-civil-manage-api-dev` namespace, so the live service is not affected.

> **Prerequisites:**
> - The dev ingress only allows requests from the MoJ VPN (except `/actuator/health`). Connect to the VPN before running any `curl` commands, otherwise ModSecurity returns `403` and the requests never reach the app.

## Auth401Unauthorized

This alert triggers when there is a spike in 401 Unauthorized responses (more than 20 within a 5-minute window).

### Step 1: Send unauthenticated requests

```bash
for i in {1..30}
do
  curl -s -o /dev/null -w "Status: %{http_code}\n" https://laa-civil-manage-api-dev.cloud-platform.service.justice.gov.uk/applications
done
```

Every request should return `Status: 401`. Within about 1–2 minutes you should get a `[FIRING:1] Auth401Unauthorized` notification in `#laa-civil-manage-alerts-non-prod`.

### Step 2: Wait for it to resolve

No clean-up is needed. The `[RESOLVED]` notification arrives once the 5-minute window passes with no further 401s.

## PodCrashLooping

This alert triggers when a container in the namespace keeps crashing and restarting. It covers every pod in the namespace, so you can test it with a throwaway pod.

### Step 1: Create a crash-looping pod

```bash
kubectl -n laa-civil-manage-api-dev run crashtest --image=busybox --restart=Always --overrides='{"spec":{"securityContext":{"runAsNonRoot":true,"runAsUser":1000,"seccompProfile":{"type":"RuntimeDefault"}},"containers":[{"name":"crashtest","image":"busybox","command":["sh","-c","exit 1"],"securityContext":{"allowPrivilegeEscalation":false,"capabilities":{"drop":["ALL"]}}}]}}'
```

Within about 5–7 minutes you should get a `[FIRING:1] PodCrashLooping` notification in Slack.

### Step 2: Delete the pod

```bash
kubectl delete pod crashtest -n laa-civil-manage-api-dev
```

The `[RESOLVED]` notification can take up to about 15–20 minutes to arrive.

## NoPodsRunning

This alert triggers when the `laa-civil-manage-api` deployment has had zero ready pods for 5 minutes.

> **Warning:** this test takes the dev environment offline while it runs. Let the team know first.

### Step 1: Scale the deployment to zero

```bash
kubectl scale deploy laa-civil-manage-api --replicas=0 -n laa-civil-manage-api-dev
```

Within about 5–6 minutes you should get a `[FIRING:1] NoPodsRunning` notification in Slack.

### Step 2: Scale the deployment back up

```bash
kubectl scale deploy laa-civil-manage-api --replicas=1 -n laa-civil-manage-api-dev
```

The pod takes about a minute to become ready, then the `[RESOLVED]` notification should arrive.
