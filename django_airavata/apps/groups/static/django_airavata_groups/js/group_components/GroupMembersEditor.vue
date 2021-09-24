<template>
  <div>
    <div class="row">
      <div class="col">
        <b-card title="Gateway Users" title-tag="h6">

          <b-form-group>
            <b-input-group>
              <b-input-group-text slot="prepend">
                <i class="fa fa-filter"></i>
              </b-input-group-text>
              <b-form-input
                v-model="userFilter"
                placeholder="Filter list of users"
                @change="onUserFilterChange"
              />
            </b-input-group>
          </b-form-group>

          <b-table
            hover
            selectable
            sticky-header
            :items="nonMembers"
            :fields="userFields"
            :filter="userFilter"
            :select-mode="multi"
            :sort-compare="sortCompare"
            responsive="sm"
            ref="usersTable"
            head-variant="light"
            sort-by="name"
            @row-selected="onUsersRowSelected"
          >
           <template slot="cell(selected)" slot-scope="data">
              <span v-if="isUserSelected(data.item)">
                <i class="far fa-check-circle"></i>
              </span>
           </template>

            <template slot="cell(action)" slot-scope="data">
              <b-button @click="toggleDetails(data)">
                {{data.detailsShowing ? 'Hide' : 'Show'}} Details
              </b-button>
            </template>

            <template slot="row-details" slot-scope="data">
              <group-members-details-container
                :userProfile="data.item"
                :name="data.item.name"
                :id="data.item.id"
                @change-role="changeRole"
              />
            </template>

          </b-table>

        </b-card>
      </div>

      <div>
        <b-button-group vertical>

          <b-button
            style="margin-top:10px; margin-bottom:10px;"
            variant="primary"
            :disabled = "selectedUsers.length<1"
            @click="addSelectedMembers">
            <!--Add Selected Members-->
            <i class="fas fa-angle-right fa-lg"></i>
          </b-button>

          <b-button
            style="margin-top:10px; margin-bottom:10px;"
            variant="primary"
            :disabled = "nonMembers.length<1"
            @click="showAdd = true">
            <!--Add All Members-->
            <i class="fas fa-angle-double-right fa-lg"></i>
          </b-button>

          <b-button
            style="margin-top:10px; margin-bottom:10px;"
            variant="primary"
            :disabled = "membersCount<2"
            @click="showRemove = true">
            <i class="fas fa-angle-double-left fa-lg"></i>
            <!--Remove All Members-->
          </b-button>

          <b-button
            style="margin-top:10px; margin-bottom:10px;"
            variant="primary"
            :disabled = "selectedMembers.length<1"
              @click="removeSelectedMembers">
              <i class="fas fa-angle-left fa-lg"></i>
              <!--Remove Selected Members-->
          </b-button>

          <b-modal
            v-model="showRemove"
            title="Are you sure?">
            <p class="my-4">
              Do you really want to remove all members from
              '<strong>{{group.name}}</strong>'?
            </p>
            <div slot="modal-footer" class="w-100">
              <b-button
                class="float-right ml-1"
                @click="removeAllMembers">
                Yes
              </b-button>
              <b-button
                class="float-right ml-1"
                @click="showRemove = false">
                No
              </b-button>
            </div>
          </b-modal>

          <b-modal
            v-model="showAdd"
            title="Are you sure?">
            <p class="my-4">
              Do you really want to add all users to
              '<strong>{{group.name}}</strong>'?
            </p>
            <div slot="modal-footer" class="w-100">
              <b-button
                class="float-right ml-1"
                @click="addAllMembers">
                Yes
              </b-button>
              <b-button
                class="float-right ml-1"
                @click="showAdd = false">
                No
              </b-button>
            </div>
          </b-modal>

        </b-button-group>
      </div>

      <div class="col">
        <b-card title="Group Members" title-tag="h6">

          <b-form-group>
            <b-input-group>
              <b-input-group-text slot="prepend">
                <i class="fa fa-filter"></i>
              </b-input-group-text>
              <b-form-input
                v-model="memberFilter"
                placeholder="Filter list of members"
                @change="onMemberFilterChange"
              />
            </b-input-group>
          </b-form-group>

          <b-table
            v-if="membersCount > 0"
            hover
            selectable
            sticky-header
            :items="currentMembers"
            :fields="memberFields"
            :filter="memberFilter"
            :select-mode="selectMode"
            :sort-compare="sortCompare"
            head-variant="light"
            responsive="sm"
            ref="membersTable"
            sort-by="name"
            @row-selected="onMembersRowSelected"
            @row-clicked="handleOwnerSelected"
          >

           <template slot="cell(selected)" slot-scope="data">
              <span v-if="isMemberSelected(data.item)">
                <i class="far fa-check-circle"></i>
              </span>
           </template>

            <template slot="cell(username)" slot-scope="data" >
              {{data.value}}
              <span
                v-if= "data.item.role == 'OWNER'"
                class="badge badge-primary">
                  Owner
              </span>
            </template>

            <template slot="cell(action)" slot-scope="data" >
              <b-button @click="toggleDetails(data)">
                {{data.detailsShowing ? 'Hide' : 'Show'}} Details
              </b-button>
            </template>

            <template slot="row-details" slot-scope="data">
              <group-members-details-container
                :userProfile="data.item"
                :name="data.item.name"
                :id="data.item.id"
                :role="data.item.role"
                :isOwner="group.isOwner"
                @change-role="changeRole"
              />
            </template>

          </b-table>

        </b-card>
      </div>
    </div>
  </div>
</template>

<script>
import { models, services } from "django-airavata-api";
import GroupMembersDetailsContainer from "./GroupMembersDetailsContainer.vue";

export default {
  name: "group-members-editor",
  components: {
    GroupMembersDetailsContainer,
  },
  props: {
    group: {
      type: models.Group,
      required: true,
    },
  },
  data() {
    return {
      userProfiles: null,
      newMembers: [],
      userFilter:null,
      memberFilter:null,
      selectedMembers: [],
      selectedUsers: [],
      showingDetails: {},
      showRemove: false,
      showAdd: false,
    };
  },

  computed: {
    members() {
      return this.group.members ? this.group.members : [];
    },
    admins() {
      return this.group.admins;
    },
    memberFields() {
      return [
        { key: "selected", label: "", sortable: false },
        { key: "username", label: "Username", sortable: true },
        { key: "action", label: "Action", sortable: false },
      ];
    },
    userFields() {
      return [
        { key: "selected", label: "", sortable: false },
        { key: "username", label: "Username", sortable: true },
        { key: "action", label: "Action", sortable: false },
      ];
    },
    userProfilesMap() {
      if (!this.userProfiles) {
        return null;
      }
      const result = {};
      this.userProfiles.forEach((up) => {
        result[up.airavataInternalUserId] = up;
      });
      return result;
    },
    currentMembers() {
      if (!this.userProfilesMap) {
        return [];
      }
      return (
        this.members
          // Filter out users that are missing profiles
          .filter((m) => m in this.userProfilesMap)
          .map((m) => {
            const userProfile = this.userProfilesMap[m];
            const isAdmin = this.admins.indexOf(m) >= 0;
            const isOwner = this.group.ownerId === m;
            // Owners can edit all members and admins can edit non-admin members
            // (except the owners role isn't editable)
            const editable =
              !isOwner &&
              (this.group.isOwner || (this.group.isAdmin && !isAdmin));
            return {
              id: m,
              name: userProfile.firstName + " " + userProfile.lastName,
              username: userProfile.userId,
              email: userProfile.email,
              role: isOwner ? "OWNER" : isAdmin ? "ADMIN" : "MEMBER",
              editable: editable,
              _showDetails: this.showingDetails[m] || false,
              _rowVariant: this.newMembers.indexOf(m) >= 0 ? "success" : null,
              _selectable: isOwner ? false : true,
            };
          })
      );
    },
    nonMembers(){
      if (!this.userProfiles) {
        return [];
      }
      return (
        this.userProfiles
          // Filter out current members
          .filter(
            (userProfile) =>
              this.group.members.indexOf(userProfile.airavataInternalUserId) < 0
          )
          .map((userProfile) => {
            return {
              id: userProfile.airavataInternalUserId,
              name: userProfile.firstName + " " + userProfile.lastName,
              username: userProfile.userId,
              email: userProfile.email,
              _showDetails: this.showingDetails[userProfile.airavataInternalUserId] || false,
            };
          })
      );
    },

    membersCount() {
      return this.members.length;
    },
  },

  created() {
    services.UserProfileService.list().then((userProfiles) => {
      this.userProfiles = userProfiles;
    });
  },

  methods: {
    toggleDetails(row) {
      row.toggleDetails();
      this.showingDetails[row.item.airavataInternalUserId] = !this
        .showingDetails[row.item.airavataInternalUserId];
    },
    isUserSelected(user){
      if (this.selectedUsers.length>0){
        for (let i = 0; i<this.selectedUsers.length;i++){
         if (user==this.selectedUsers[i]){
           return true;
          }
        }
      }
      return false;
    },
    isMemberSelected(member){
      if (this.selectedMembers.length>0){
        for (let i = 0; i<this.selectedMembers.length;i++){
         if (member==this.selectedMembers[i]){
           return true;
          }
        }
      }
      return false;
    },
    addSelectedMembers(){
      this.selectedUsers.forEach((user)=>  {
        this.newMembers.push(user.id);
        this.$emit("add-member", user.id);
        }
      );
      this.$refs.usersTable.clearSelected();
      this.$refs.membersTable.clearSelected();
      this.selectedUsers=[];
      this.selectedMembers=[];
    },
    addAllMembers(){
      this.showAdd = false;
      this.selectedUsers = this.nonMembers.filter(
      (user) =>
              this.filterUserProfile(user, this.userFilter)
          ).map((x)=>(x));
      this.addSelectedMembers();
      this.userFilter=null;
      this.memberfilter=null;
    },
    removeSelectedMembers() {
      this.selectedMembers.forEach((member)=>{
         if (member.role == "MEMBER"|| member.role =="ADMIN"){
          this.$emit("remove-member", member.id);
        }});
      this.$refs.membersTable.clearSelected();
      this.selectedMembers = [];
      this.selectedUsers=[];
    },
    removeAllMembers(){
      this.showRemove = false;
      this.selectedMembers = this.currentMembers.filter(
      (member) =>
              this.filterUserProfile(member, this.memberFilter)
          ).map((x)=>(x));
      this.removeSelectedMembers();
      this.memberFilter=null;
      this.userFields=null;
    },
    onMembersRowSelected(items){
      this.selectedMembers = items;
      if (this.selectedUsers){
        this.$refs.usersTable.clearSelected();
        this.selectedUsers = [];
      }
    },
    onUsersRowSelected(items){
      this.selectedUsers = items;
      if (this.selectedMembers){
        this.$refs.membersTable.clearSelected();
        this.selectedMembers = [];
      }
    },
    onUserFilterChange(){
      this.selectedUsers = [];
    },
    onMemberFilterChange(){
      this.selectedMembers = [];
    },
    changeRole(item) {
      if (item[1] === "ADMIN") {
        this.$emit("change-role-to-admin", item[0]);
      } else {
        this.$emit("change-role-to-member", item[0]);
      }
    },
    filterUserProfile(profile, filter){
      if(filter){
      if (profile.email.toLowerCase().includes(filter.toLowerCase())){
        return true;
      }
      else if(profile.name.toLowerCase().includes(filter.toLowerCase())){
        return true;
      }else if(profile.username.toLowerCase().includes(filter.toLowerCase())){
        return true;
      }else{return false;}
      }else{
        return true;
      }
    },
    handleOwnerSelected(item, index){
      if(!item._selectable){
        this.$refs.membersTable.selectRow(index);
      }
    },
    sortCompare(aRow, bRow, key) {
      // Sort new members before all others
      const aNewIndex = this.newMembers.indexOf(aRow.id);
      const bNewIndex = this.newMembers.indexOf(bRow.id);
      if (aNewIndex >= 0 && bNewIndex >= 0) {
        return aNewIndex - bNewIndex;
      } else if (aNewIndex >= 0) {
        return -1;
      } else if (bNewIndex >= 0) {
        return 1;
      }
      const a = aRow[key];
      const b = bRow[key];
      if (
        (typeof a === "number" && typeof b === "number") ||
        (a instanceof Date && b instanceof Date)
      ) {
        // If both compared fields are native numbers or both are dates
        return a < b ? -1 : a > b ? 1 : 0;
      } else {
        // Otherwise stringify the field data and use String.prototype.localeCompare
        return new String(a).localeCompare(new String(b));
      }
    },
  },
};
</script>
