package org.apache.airavata.xbaya.component.registry.jackrabbit.user;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.Group;

public class AbstractJackRabbitUMComponent {

	protected List<Group> getJRGroupList(
			Iterator<org.apache.airavata.registry.api.user.Group> airavataUMGroupList) {
		List<Group> groupList=new ArrayList<Group>();
		while(airavataUMGroupList.hasNext()){
			groupList.add(new JackRabbitGroup(airavataUMGroupList.next()));
		}
		return groupList;
	}
	
	protected List<org.apache.airavata.registry.api.user.Group> getGroupList(
			Iterator<Group> airavataUMGroupList) {
		List<org.apache.airavata.registry.api.user.Group> groupList=new ArrayList<org.apache.airavata.registry.api.user.Group>();
		while(airavataUMGroupList.hasNext()){
			groupList.add(new JackRabbitGroupWrap(airavataUMGroupList.next()));
		}
		return groupList;
	}
	
	protected List<Authorizable> getJRAuthorizableList(
			Iterator<org.apache.airavata.registry.api.user.Authorizable> jackRabbitAuthorizableList) {
		List<Authorizable> authorizableList = new ArrayList<Authorizable>();
		while(jackRabbitAuthorizableList.hasNext()) {
			authorizableList.add(new JackRabbitAuthorizable(jackRabbitAuthorizableList.next()));
		}
		return authorizableList;
	}
	
	protected List<org.apache.airavata.registry.api.user.Authorizable> getAuthorizableList(
			Iterator<Authorizable> jackRabbitAuthorizableList) {
		List<org.apache.airavata.registry.api.user.Authorizable> authorizableList = new ArrayList<org.apache.airavata.registry.api.user.Authorizable>();
		while(jackRabbitAuthorizableList.hasNext()) {
			authorizableList.add(new JackRabbitAuthorizableWrap(jackRabbitAuthorizableList.next()));
		}
		return authorizableList;
	}
}
