/**
 * 
 */
package helpers

/**
 * @author scarlett
 *
 */
class ProjectNotificationsHelper {
	static Closure createEmailNotifications(String Jobname, String recipients) {
		def Trigger = {			
				email {
					recipientList "sgclark@kde.org" + ', ' + recipients
					subject 'Jenkins-kde-ci: ${PROJECT_NAME} - Build # ${BUILD_NUMBER} - ${BUILD_STATUS}!'
					body '${JELLY_SCRIPT,template="text"}'									
				}
			}		
		def DEFAULT_RECIPIENTS = "sgclark@kde.org"
		return { project ->
				project / publishers << 'hudson.plugins.emailext.ExtendedEmailPublisher' {
					recipientList DEFAULT_RECIPIENTS + ', ' + recipients
					attachmentsPattern
					attachBuildLog false
					compressBuildLog false
					replyTo "no-reply@kde.org"
					matrixTriggerMode 'ONLY_CONFIGURATIONS'					  
					configuredTriggers {						
						'hudson.plugins.emailext.plugins.trigger.FixedTrigger' Trigger
						'hudson.plugins.emailext.plugins.trigger.StatusChangedTrigger' Trigger
						'hudson.plugins.emailext.plugins.trigger.StillFailingTrigger' Trigger
						'hudson.plugins.emailext.plugins.trigger.StillUnstableTrigger' Trigger						
					}
					contentType 'default'
					defaultSubject 'KDE Jenkins CI report for ' + Jobname
					defaultContent 'Auto generated KDE Jenkins CI report for ' + Jobname
				}
			}		
	}
	static Closure createIRCNotifications(String ircchannel) {
		return { project ->
			project / publishers << 'hudson.plugins.ircbot.IrcPublisher' {
				targets {
					 'hudson.plugins.im.GroupChatIMMessageTarget' {						 
						 name "#kde-builds"					
						 notificationOnly false
					 }
					 if (ircchannel != "#kde-builds" && ircchannel != null)	{
					 'hudson.plugins.im.GroupChatIMMessageTarget' {					 			 
						 name ircchannel
						 notificationOnly false
					 	}					 
					 }
				}
				strategy 'ALL'
				notifyOnBuildStart false
				notifySuspects false
				notifyCulprits false
				notifyFixers false
				notifyUpstreamCommitters false
				buildToChatNotifier(class: "hudson.plugins.im.build_notify.DefaultBuildToChatNotifier") {
/*					matrixMultiplier 'ONLY_CONFIGURATIONS'
					channels "#kde-builds"
					channels ircchannels*/
				}
			}	
		}
	}
}
