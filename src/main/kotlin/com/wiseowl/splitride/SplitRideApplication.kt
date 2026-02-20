package com.wiseowl.splitride

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class SplitRideApplication
fun main(args: Array<String>) {
    runApplication<SplitRideApplication>(*args)
}
