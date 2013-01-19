package com.thishood.mail

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * This a async (works via queue) replacement of {@link grails.plugin.mail.MailService)
 * @see com.thishood.mail.QueueMailProcessorService
 */
class QueueMailerService {

    boolean transactional = false
    def templateRenderService

    def sendEmail(attrs) {
        def text = templateRenderService.render(attrs.view, attrs.model)
        //todo vitaliy@22.02.11 add additional method for multipart messages???
        sendJMSMessage("queue.mail",
                [
                        to: attrs.to,
                        from: attrs.from ?: getFromEmail(),
                        subject: attrs.subject,
                        body: text
                ]
        )
    }

    /**
     * Default from email from config
     */
    def getFromEmail() {
        AH.application.config.grails.mail.from
    }
}

