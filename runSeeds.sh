#!/bin/bash

chmod +x ./seeds/seed_group.sh
chmod +x ./seeds/seed_cocktails.sh
chmod +x ./seeds/seed_cocktailGroupLinks.sh

echo "run seed_group.sh"
sh ./seeds/seed_group.sh
echo "run seed_cocktails.sh"
sh ./seeds/seed_cocktails.sh
echo "run seed_cocktailGroupLinks.sh"
sh ./seeds/seed_cocktailGroupLinks.sh

echo "Seeding completed."

# aws dynamodb scan --table-name cocktail_fellow_local_group --endpoint-url http://localhost:8000
# aws dynamodb scan --table-name cocktail_fellow_local_cocktail --endpoint-url http://localhost:8000
# aws dynamodb scan --table-name  cocktail_fellow_local_cocktailGroupLink --endpoint-url http://localhost:8000
