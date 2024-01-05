package com.cocktailfellow.cocktail

import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.ValidationException
import com.cocktailfellow.common.link.CocktailGroupLinkRepository

class CocktailService {
  private val cocktailRepository: CocktailRepository = CocktailRepository()
  private val cocktailGroupLinkRepository: CocktailGroupLinkRepository = CocktailGroupLinkRepository()

  fun createCocktail(groupId: String, cocktail: Cocktail) {

    cocktailRepository.createCocktail(cocktail)
    cocktailGroupLinkRepository.createCocktailToGroupLink(groupId, cocktail.cocktailId)
  }

  fun updateCocktail(cocktail: Cocktail) {
    cocktailRepository.updateCocktail(cocktail)
  }

  fun getCocktails(groupId: String): List<CocktailInfo> {
    val items = cocktailGroupLinkRepository.fetchItems(groupId)

    return items.map { item ->
      val cocktailId =
        item["cocktailId"]?.s() ?: throw ValidationException("CocktailId is missing for group: $groupId")
      val cocktail: Cocktail = cocktailRepository.getCocktail(cocktailId)
      CocktailInfo(
        cocktailId = cocktail.cocktailId,
        name = cocktail.name,
        method = cocktail.method
      )
    }
  }

  fun getCocktail(cocktailId: String): Cocktail {
    return cocktailRepository.getCocktail(cocktailId)
  }

  fun getCocktailsIngredients(groupId: String): List<CocktailIngredients> {
    val cocktails = cocktailGroupLinkRepository.fetchItems(groupId)

    return cocktails.map { cocktail ->
      val cocktailId =
        cocktail["cocktailId"]?.s() ?: throw ValidationException("CocktailId is missing for group: $groupId")
      val cocktailResponse: Cocktail = cocktailRepository.getCocktail(cocktailId)
      CocktailIngredients(cocktailId = cocktailResponse.cocktailId,
        name = cocktailResponse.name,
        method = cocktailResponse.method!!,
        ingredients = cocktailResponse.ingredients)
    }
  }

  fun deleteCocktail(cocktailId: String) {
    return cocktailRepository.deleteCocktail(cocktailId)
  }
}
