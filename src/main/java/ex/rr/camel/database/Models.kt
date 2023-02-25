package ex.rr.camel.database

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.time.LocalDateTime

@Entity
@Table(name = "T_ORDER")
data class Order(
    @Id @GeneratedValue val id: Long? = null,
    var status: Status? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: String? = LocalDateTime.now().toString(),
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    val completedAt: String? = null,
    @OneToMany(cascade = [CascadeType.MERGE, CascadeType.ALL])
    @Fetch(FetchMode.JOIN)
    val users: List<User> = listOf(),
    @OneToOne(cascade = [CascadeType.ALL])
    val payment: Payment? = null,
    @OneToMany(cascade = [CascadeType.ALL])
    @Fetch(FetchMode.JOIN)
    val items: List<Item> = listOf()
)

@Entity
@Table(name = "T_USER")
data class User(
    @Id @GeneratedValue val id: Long? = null,
    val type: UserType? = null
)

@Entity
@Table(name = "T_PAYMENT")
data class Payment(
    @Id @GeneratedValue val id: Long? = null,
    val type: String? = null,
    val amount: Double? = null,
    val currency: Currency? = null
)

@Entity
@Table(name = "T_ITEM")
data class Item(
    @Id @GeneratedValue val id: Long? = null,
    val action: Action? = null,
    val type: ItemType? = null,
    var status: Status? = Status.PENDING,
    @OneToMany(cascade = [CascadeType.ALL])
    @Fetch(FetchMode.JOIN)
    val characteristics: MutableList<Characteristic> = mutableListOf()
)

@Entity
@Table(name = "T_CHARACTERISTIC")
data class Characteristic(
    @JsonIgnore
    @Id @GeneratedValue val id: Long? = null,
    val name: String? = null,
    @Column(name = "T_value")
    val value: String? = null
)

enum class Status {
    PENDING, COMPLETED, FAILED
}

enum class Action {
    ADD, DELETE, NONE
}

enum class ItemType {
    COFFEE, GRINDER, SUBSCRIPTION
}

enum class UserType {
    OWNER, AGENT
}

enum class Currency {
    USD, EUR
}


@Entity
data class ProcessingConfirmation(
    @Id @GeneratedValue val id: Long? = null,
    val orderId: Long?,
    val orderItemId: Long?,
    val action: Action?,
    var status: Status?
)