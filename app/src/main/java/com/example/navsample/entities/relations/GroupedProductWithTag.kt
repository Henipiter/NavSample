package com.example.navsample.entities.relations

import com.example.navsample.entities.database.Tag


data class GroupedProductWithTag(
    var id: String,
    var tag: List<Tag>
) {

    companion object {
        fun convert(
            productId: String,
            productWithTag: List<ProductWithTag>
        ): GroupedProductWithTag {
            val products = productWithTag.filter { it.id == productId }
            val tags = products.map { getTag(it.tagId ?: "", it.tagName) }
            return GroupedProductWithTag(productId, tags)
        }

        private fun getTag(id: String, name: String): Tag {
            val tag = Tag(name)
            tag.id = id
            return tag
        }
    }

}