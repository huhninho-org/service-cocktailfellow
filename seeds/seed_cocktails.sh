#!/bin/bash

aws dynamodb put-item --table-name cocktail_fellow_local_cocktail --item '{
    "cocktailId": {"S": "default-bellini"},
    "data": {"S": "{\"cocktailId\":\"default-bellini\",\"name\":\"Bellini\",\"method\":\"Pour peach puree into the mixing glass with ice, add the Prosecco wine. Stir gently and pour in a chilled flute glass.\",\"notes\":\"Variations include Puccini (with fresh mandarin orange juice), Rossini (with fresh strawberry puree), and Tintoretto (with fresh pomegranate juice).\",\"ingredients\":[{\"ingredientName\":\"Prosecco\",\"amount\":\"100 ml\"},{\"ingredientName\":\"White Peach Puree\",\"amount\":\"50 ml\"}]}"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktail --item '{
    "cocktailId": {"S": "default-black_russian"},
    "data": {"S": "{\"cocktailId\":\"default-black_russian\",\"name\":\"Black Russian\",\"method\":\"Pour the ingredients into the old fashioned glass filled with ice cubes. Stir gently.\",\"notes\":\"For a White Russian, float fresh cream on the top and stir in slowly.\",\"ingredients\":[{\"ingredientName\":\"Vodka\",\"amount\":\"50 ml\"},{\"ingredientName\":\"Coffee Liqueur\",\"amount\":\"20 ml\"}]}"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktail --item '{
    "cocktailId": {"S": "default-bloody_mary"},
    "data": {"S": "{\"cocktailId\":\"default-bloody_mary\",\"name\":\"Bloody Mary\",\"method\":\"Stir gently all the ingredients in a mixing glass with ice, pour into rocks glass.\",\"notes\":\"If requested served with ice, pour into highball glass.\",\"ingredients\":[{\"ingredientName\":\"Vodka\",\"amount\":\"45 ml\"},{\"ingredientName\":\"Tomato Juice\",\"amount\":\"90 ml\"},{\"ingredientName\":\"Fresh Lemon Juice\",\"amount\":\"15 ml\"},{\"ingredientName\":\"Worcestershire Sauce\",\"amount\":\"2 dashes\"},{\"ingredientName\":\"Tabasco, Celery Salt, Pepper\",\"amount\":\"Up to taste\"}]}"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktail --item '{
    "cocktailId": {"S": "default-caipirinha"},
    "data": {"S": "{\"cocktailId\":\"default-caipirinha\",\"name\":\"Caipirinha\",\"method\":\"Place lime and sugar into a double old fashioned glass and muddle gently. Fill the glass with cracked ice and add Cachaça. Stir gently to involve ingredients.\",\"notes\":\"Caipiroska uses vodka instead of Cachaça.\",\"ingredients\":[{\"ingredientName\":\"Cachaça\",\"amount\":\"60 ml\"},{\"ingredientName\":\"Lime\",\"amount\":\"1 cut into small wedges\"},{\"ingredientName\":\"White Cane Sugar\",\"amount\":\"4 teaspoons\"}]}"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000

aws dynamodb put-item --table-name cocktail_fellow_local_cocktail --item '{
    "cocktailId": {"S": "default-champagne_cocktail"},
    "data": {"S": "{\"cocktailId\":\"default-champagne_cocktail\",\"name\":\"Champagne Cocktail\",\"method\":\"Place the sugar cube with 2 dashes of bitters in a large Champagne glass, add the cognac. Pour gently chilled Champagne.\",\"notes\":\"\",\"ingredients\":[{\"ingredientName\":\"Chilled Champagne\",\"amount\":\"90 ml\"},{\"ingredientName\":\"Cognac\",\"amount\":\"10 ml\"},{\"ingredientName\":\"Angostura Bitters\",\"amount\":\"2 dashes\"},{\"ingredientName\":\"Grand Marnier\",\"amount\":\"Few drops (optional)\"},{\"ingredientName\":\"Sugar Cube\",\"amount\":\"1\"}]}"},
    "isProtected": {"BOOL": true}
}' --endpoint-url http://localhost:8000
