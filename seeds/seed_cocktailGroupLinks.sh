#!/bin/bash

aws dynamodb put-item --table-name cocktail_fellow_local_cocktailGroupLink --item '{
    "id": {"S": "default-bellini-default-iba-id"},
    "cocktailId": {"S": "default-bellini"},
    "groupId": {"S": "default-iba-id"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktailGroupLink --item '{
    "id": {"S": "default-champagne_cocktail-default-iba-id"},
    "cocktailId": {"S": "default-champagne_cocktail"},
    "groupId": {"S": "default-iba-id"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktailGroupLink --item '{
    "id": {"S": "default-bloody_mary-default-iba-id"},
    "cocktailId": {"S": "default-bloody_mary"},
    "groupId": {"S": "default-iba-id"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktailGroupLink --item '{
    "id": {"S": "default-black_russian-default-iba-id"},
    "cocktailId": {"S": "default-black_russian"},
    "groupId": {"S": "default-iba-id"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktailGroupLink --item '{
    "id": {"S": "default-caipirinha-default-iba-id"},
    "cocktailId": {"S": "default-caipirinha"},
    "groupId": {"S": "default-iba-id"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000
