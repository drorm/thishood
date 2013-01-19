package com.thishood

import com.thishood.domain.PersistableTemplate
import com.thishood.domain.UserGroup

class PersistableTemplateService extends AbstractHolderableService{

    static transactional = true

    PersistableTemplate findByUserGroup(Long userGroupId) {
		UserGroup userGroup = UserGroup.getOrFail(userGroupId)
		PersistableTemplate persistableTemplate = PersistableTemplate.find("from PersistableTemplate pt where pt.userGroup = :userGroup", [userGroup:userGroup])

		if (!persistableTemplate) {
			persistableTemplate = new PersistableTemplate(
					userGroup: userGroup,
					content: getDefaultTemplate(),
					dateCreated: new Date()
			).save(failOnError:true, flush:true)
		}

		persistableTemplate
    }

	String getDefaultTemplate() {
		'''

		${greeting}
		I live on your street at ${from.address}.
		I want to get our block connected online by using ThisHood, a secure web application that will make communication simple for us as a group.
		Our fellow Albany citizens are already using ThisHood to recommend babysittersr, advertise available apartments, and share local police reports.
		As a block ThisHood will make it a lot easier to plan block parties, work together on disaster preparedness, organize a neighborhood watch and share goods and services in a local marketplace.

		ThisHood's founder is Albany resident <b>Dror Matalon</b> who also created and moderates the Albany School's mailing list.
		Staying true to ThisHood's focus on local community, Dror decided to deploy first in his own city, Albany.
		This means that as the first ThisHood users we have a unique chance to give feedback on what we think will work best for our city.
		I'm looking forward to connecting with you using ThisHood.

		Sign up <a href="${url}">${url}</a>

		Learn more: <a href="http://www.thishood.com/home/">http://www.thishood.com/home/</a>

		Your Neighbor,
		${from.displayName}

		ThisHood builds strong local communities.


		'''.stripIndent()
	}
}
