<template>
    <div style="position:relative">
        <span class="selected-cards" style="position:relative">
          <b-button variant="warning" v-for="item in selected" v-bind:key="item.id" @click="removeClick(item)">
             {{ item.name }} <b-badge variant="light"><a href="#">x</a></b-badge>
          </b-button>
        </span>
        <hr>
        <autocomplete-text-input :suggestions="suggestions" @selected="suggestionSelected"/>
    </div>
</template>

<script>
import AutocompleteTextInput from './AutocompleteTextInput.vue'

export default {

  name: 'autocomplete',
  props: {
    value: {
      type: Array,
      required: false
        , default:[]
    },

    suggestions: {
      type: Array,
      required: true
    }
  },
  data () {
      console.log("Value",this.value.length);
    return {
      localValue: this.value,
    }
  },
  components: {
      AutocompleteTextInput,
  },
  computed: {
    selected () {
        console.log("local Value",this.localValue.length);
        return this.suggestions.filter((suggestion) => {
            return this.localValue.indexOf(suggestion.id) >= 0;
        });
    }
  },
  methods: {
    suggestionSelected (suggestion) {
      if(this.localValue.indexOf(suggestion.id)==-1) {
        this.localValue.push(suggestion.id);
      }
      this.$emit('input',this.localValue);
    },
    removeClick(data) {
      var index = this.localValue.indexOf(data.id);
      this.localValue.splice(index,1);
      this.$emit('input',this.localValue);
    }
  },
    watch:{
      value:function (newValue) {
          this.localValue=newValue;
      }
    }
}

</script>
<style>
.selected-cards > button + button {
    margin-left: 10px;
}
</style>
