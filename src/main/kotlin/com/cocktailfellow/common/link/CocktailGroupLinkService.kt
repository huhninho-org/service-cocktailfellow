package com.cocktailfellow.common.link

class CocktailGroupLinkService {
  private val cocktailGroupLinkRepository: CocktailGroupLinkRepository = CocktailGroupLinkRepository()

  fun isMemberOfGroup(cocktailId: String, groupId: String): Boolean {
    return cocktailGroupLinkRepository.isMemberOfGroup(cocktailId, groupId)
  }

  fun deleteLink(cocktailId: String, groupId: String) {
    return cocktailGroupLinkRepository.deleteLink(cocktailId, groupId)
  }
}
