package ex.rr.camel.database

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderRepository : JpaRepository<Order, Long>

@Repository
interface ItemRepository : JpaRepository<Item, Long>

@Repository
interface ProcessingConfirmationRepository : JpaRepository<ProcessingConfirmation, Long>