package com.cocktailfellow.cocktail

import com.cocktailfellow.cocktail.database.CocktailRepository
import com.cocktailfellow.cocktail.model.Cocktail
import com.cocktailfellow.cocktail.model.CocktailInfo
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

  fun isProtected(cocktailId: String): Boolean {

    val a = cocktailRepository.getCocktail(cocktailId).isProtected
    print("DEBUG: isProtected:$a")

    return cocktailRepository.getCocktail(cocktailId).isProtected ?: false
  }

  fun getCocktail(cocktailId: String): Cocktail {
    return cocktailRepository.getCocktail(cocktailId)
  }

  fun getCocktails(groupId: String): List<CocktailInfo> {
    val cocktails = cocktailGroupLinkRepository.fetchItems(groupId)

    return cocktails.map { cocktail ->
      val cocktailId =
        cocktail["cocktailId"]?.s() ?: throw ValidationException("CocktailId is missing for group: $groupId")
      val cocktailResponse: Cocktail = cocktailRepository.getCocktail(cocktailId)
      CocktailInfo(cocktailId = cocktailResponse.cocktailId,
        name = cocktailResponse.name,
        method = cocktailResponse.method!!,
        ingredients = cocktailResponse.ingredients,
        isProtected = cocktailResponse.isProtected)
    }
  }

  fun deleteCocktail(cocktailId: String) {
    return cocktailRepository.deleteCocktail(cocktailId)
  }
}
