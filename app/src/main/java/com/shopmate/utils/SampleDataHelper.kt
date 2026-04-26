package com.shopmate.utils

import com.shopmate.data.entities.Sale
import com.shopmate.data.entities.SaleTransaction
import com.shopmate.data.repository.ProductRepository
import com.shopmate.data.repository.SaleRepository
import java.time.LocalDateTime
import java.util.UUID

object SampleDataHelper {

    /**
     * Seeds sample products AND generates realistic sales history
     * across the past 30 days — useful for demo / first launch.
     */
    suspend fun seedAll(
        productRepo: ProductRepository,
        saleRepo: SaleRepository
    ) {
        // 1. Insert sample products
        productRepo.insertSampleProducts()

        // 2. Fetch newly inserted products
        val products = productRepo.getAllProductsSync()
        if (products.isEmpty()) return

        val rand = java.util.Random(42)
        val paymentMethods = listOf("Cash", "UPI", "Card")
        val now = LocalDateTime.now()

        // 3. Generate ~5-15 sales per day for the past 14 days
        repeat(14) { daysAgo ->
            val dayBase = now.minusDays(daysAgo.toLong())
            val salesThisDay = 5 + rand.nextInt(11) // 5–15

            repeat(salesThisDay) { txIndex ->
                val txId = UUID.randomUUID().toString()
                val hour = 8 + rand.nextInt(12) // 8am – 8pm
                val minute = rand.nextInt(60)
                val saleTime = dayBase
                    .withHour(hour)
                    .withMinute(minute)
                    .withSecond(0)
                    .toString()

                // Pick 1-3 random products for this transaction
                val itemCount = 1 + rand.nextInt(3)
                val chosenProducts = products.shuffled().take(itemCount)
                val payment = paymentMethods[rand.nextInt(paymentMethods.size)]

                val saleItems = chosenProducts.map { p ->
                    val qty = 1 + rand.nextInt(5)
                    Sale(
                        productId = p.id,
                        productName = p.name,
                        category = p.category,
                        quantitySold = qty,
                        sellingPrice = p.sellingPrice,
                        costPrice = p.costPrice,
                        totalAmount = p.sellingPrice * qty,
                        profit = (p.sellingPrice - p.costPrice) * qty,
                        discount = 0.0,
                        paymentMethod = payment,
                        saleDate = saleTime,
                        transactionId = txId
                    )
                }

                val txTotal = saleItems.sumOf { it.totalAmount }
                val txProfit = saleItems.sumOf { it.profit }

                saleRepo.insertSales(saleItems)
                saleRepo.insertTransaction(
                    SaleTransaction(
                        transactionId = txId,
                        totalAmount = txTotal,
                        totalProfit = txProfit,
                        discount = 0.0,
                        paymentMethod = payment,
                        createdAt = saleTime
                    )
                )
            }
        }
    }
}
