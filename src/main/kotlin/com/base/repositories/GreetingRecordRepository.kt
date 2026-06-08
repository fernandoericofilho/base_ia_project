package com.base.repositories

import com.base.models.GreetingRecord
import org.springframework.data.jpa.repository.JpaRepository

interface GreetingRecordRepository : JpaRepository<GreetingRecord, Long>

