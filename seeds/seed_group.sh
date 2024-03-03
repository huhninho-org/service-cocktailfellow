#!/bin/bash

aws dynamodb put-item \
    --table-name cocktail_fellow_local_group   \
     --item '{
         "groupId": {"S": "default-iba-id"},
         "groupname": {"S": "International Bar Association"},
         "isProtected": {"BOOL": true}
     }' \
     --endpoint-url http://localhost:8000
