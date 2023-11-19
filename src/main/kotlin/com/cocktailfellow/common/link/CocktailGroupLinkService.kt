package com.cocktailfellow.common.link

import com.cocktailfellow.cocktail.model.CocktailInfo
import com.cocktailfellow.cocktail.model.CocktailIngredients
import com.cocktailfellow.common.link.CocktailGroupLinkRepository

class CocktailGroupLinkService {
  private val cocktailGroupLinkRepository: CocktailGroupLinkRepository = CocktailGroupLinkRepository()

  fun createCocktailToGroupLink(cocktailId: String, groupId: String) {
    return cocktailGroupLinkRepository.createCocktailToGroupLink(cocktailId, groupId)
  }

  fun isMemberOfGroup(cocktailId: String, groupId: String): Boolean {
    return cocktailGroupLinkRepository.isMemberOfGroup(cocktailId, groupId)
  }

  fun getCocktails(groupId: String): List<CocktailInfo> {
    return cocktailGroupLinkRepository.getCocktails(groupId)
  }

  fun getCocktailsIngredients(groupId: String): List<CocktailIngredients> {
    return cocktailGroupLinkRepository.getCocktailsIngredients(groupId)
  }

  fun deleteLink(cocktailId: String, groupId: String) {
    return cocktailGroupLinkRepository.deleteLink(cocktailId, groupId)
  }

}
