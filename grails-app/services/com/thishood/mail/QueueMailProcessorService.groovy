package com.thishood.mail

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH

/**
 * ActiveMQ service which works as callback via <code>onMessage(emailMessage)</code> method listener
 */
class QueueMailProcessorService {

    boolean transactional = false

    //Tell the JMS plugin that this is a message endpoint
    static expose = ['jms']
    static destination = "queue.mail"

    //The Mail plugin service
    def mailService

    /**
     * Notice that the onMessage method is returning null – this is important as it tells the MessageListenerAdapter that the method has run successfully.
     * If you return anything else it will be assumed it’s a retry message and you could end up with a poison messaged in your queue.
     *
     * @param emailMessage
     * @return
     */
    def onMessage(emailMessage) {
        if (isEnabled()) {
            try {
                mailService.sendMail {
                    to emailMessage.to
                    from emailMessage.from
                    subject emailMessage.subject
                    //body(view: emailMessage.view, model: emailMessage.model)
                    html emailMessage.body

                }
            } catch (Exception e) {
                log.error("Failed to send email ${emailMessage}", e)
            }
        } else {
            log.info "queue email sending is disabled"
        }

        //Return null to avoid poison messages
        return null
    }

	def isEnabled() {
		AH.application.config.com.thishood.queue.enable
	}
}
