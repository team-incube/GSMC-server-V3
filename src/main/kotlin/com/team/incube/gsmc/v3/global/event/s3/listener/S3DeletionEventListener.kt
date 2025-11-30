package com.team.incube.gsmc.v3.global.event.s3.listener

import com.team.incube.gsmc.v3.global.event.s3.S3BulkFileDeletionEvent
import com.team.incube.gsmc.v3.global.event.s3.S3FileDeletionEvent
import com.team.incube.gsmc.v3.global.thirdparty.aws.s3.service.S3DeleteService
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class S3DeletionEventListener(
    private val s3DeleteService: S3DeleteService,
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleS3FileDeletion(event: S3FileDeletionEvent) {
        s3DeleteService.execute(event.fileUri)
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleS3BulkFileDeletion(event: S3BulkFileDeletionEvent) {
        s3DeleteService.execute(event.fileUris)
    }
}
