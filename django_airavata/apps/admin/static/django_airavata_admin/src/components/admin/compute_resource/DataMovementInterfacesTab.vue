<template>
  <div class="main_section">
    <div class="new-application-tab-main">
      <h4>Data Movement Interfaces</h4>
      <div class="entry" v-if="editable">
        <div class="heading">Add Job Submission</div>
        <select v-model="dataMovement" v-bind:disabled="editable?'':'disabled'">
          <option value="0">LOCAL</option>
          <option value="1">SCP</option>
          <option value="3">GridFTP</option>
          <option value="4">UNICORE_STORAGE_SERVICE</option>
        </select>
        <div class="submission-btn">
          <input type="button" class="deployment btn" value="Add Job Submission" v-on:click="addDataMovementInterface()" />
        </div>
      </div>
    </div>
    <div class="new-application-tab-main">
      <tab-sub-section v-for="interface_,index in data.dataMovementInterfaces" v-bind:key="index">
        <l-o-c-a-l-data-movement v-if="interface_.dataMovementProtocol == 0" v-bind:id="interface_.dataMovementInterfaceId"></l-o-c-a-l-data-movement>
        <s-c-p-data-movement v-else-if="interface_.dataMovementProtocol == 1" v-bind:id="interface_.dataMovementInterfaceId"></s-c-p-data-movement>
        <grid-f-t-p-data-movement v-else-if="interface_.dataMovementProtocol == 3" v-bind:id="interface_.dataMovementInterfaceId"></grid-f-t-p-data-movement>
        <unicore-data-movement v-else-if="interface_.dataMovementProtocol == 4" v-bind:id="interface_.dataMovementInterfaceId"></unicore-data-movement>
      </tab-sub-section>
    </div>
  </div>
</template>
<script>
import tabMixin from "../../tabs/tab_mixin";
import computeResourceTabMixin from "./compute_resource_tab_mixin";

import TabSubSection from "../../tabs/TabSubSection";
import SCPDataMovement from "./data_movement/SCPDataMovement";
import GridFTPDataMovement from "./data_movement/GridFTPDataMovement";
import LOCALDataMovement from "./data_movement/LOCALDataMovement";
import UnicoreDataMovement from "./data_movement/UnicoreDataMovement";

import { mapActions, mapGetters, mapMutations } from "vuex";

export default {
  name: "data-movement-interfaces-tab",
  components: {
    UnicoreDataMovement,
    GridFTPDataMovement,
    SCPDataMovement,
    LOCALDataMovement,
    TabSubSection
  },
  mixins: [tabMixin, computeResourceTabMixin],
  computed: {
    ...mapGetters({ counter: "computeResource/counter" })
  },
  data: function() {
    return {
      data: {},
      dataMovement: null
    };
  },
  methods: {
    addDataMovementInterface: function() {
      let id = this.counter();
      switch (parseInt(this.dataMovement)) {
        case 0:
          this.addLocalJob(id);
          break;
        case 1:
          this.addScp(id);
        case 3:
          this.addGrid(id);
          break;
        case 4:
          this.addUnicore(id);
          break;
      }
      if (this.dataMovement) {
        this.addDataMovement({ id: id, protocol: parseInt(this.dataMovement) });
      }
    },
    ...mapMutations({
      addLocal: "computeResource/dataMovement/addLocal",
      addUnicore: "computeResource/dataMovement/addUnicore",
      addGrid: "computeResource/dataMovement/addGrid",
      addScp: "computeResource/dataMovement/addScp",
      addDataMovement: "computeResource/addDataMovement"
    })
  }
};
</script>

<style scoped>
.submission-btn input {
  margin-top: 10px;
}
</style>
