<template>
    <div style="position:relative">
        <span class="selected-cards" style="position:relative">
          <b-button variant="warning" v-for="item in selected" v-bind:key="item.id" @click="removeClick(item)">
             {{ item.name }} <b-badge variant="light"><a href="#">x</a></b-badge>
          </b-button>
        </span>
        <hr>
        <input class="form-control" type="text" :value="searchValue" placeholder="Type to get suggestions..." @input="updateSearchValue($event.target.value)"
          @keydown.enter = 'enter'
          @keydown.down = 'down'
          @keydown.up = 'up'
        >
        <b-list-group style="width: 100%;" v-if="open">
            <b-list-group-item v-for="(suggestion, index) in filtered.slice(0,5)" v-bind:class="{'active': isActive(index)}" href="#" @click="suggestionClick(index)" v-bind:key="suggestion.id">
              {{ suggestion.name }}
            </b-list-group-item>
        </b-list-group>
    </div>
</template>

<script>

export default {

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
      open: false,
      current: 0,
      localValue: this.value,
      searchValue: '',
    }
  },

  computed: {
    filtered () {
        console.log("local Value",this.localValue.length);
      return this.suggestions.filter((data) => {
        return data.name.indexOf(this.searchValue) >= 0
      })
    },
    selected () {
        console.log("local Value",this.localValue.length);
        return this.suggestions.filter((suggestion) => {
            return this.localValue.indexOf(suggestion.id) >= 0;
        });
    }
  },
  methods: {
    updateSearchValue (value) {
      if (this.open === false) {
        this.open = true
        this.current = 0
      }
      if(value===''){
        this.open = false;
      }
      this.searchValue = value;
    },
    enter () {
      var index = this.suggestions.indexOf(this.filtered[this.current].name);
      if(this.localValue.indexOf(this.filtered[this.current].id)==-1){
        this.localValue.push(this.filtered[this.current].id);
      }
      this.$emit('input',this.localValue);
      this.searchValue = '';
      this.open = false
    },
    up () {
      if (this.current > 0) {
        this.current--
      }
    },
    down () {
      if (this.current < this.filtered.length - 1) {
        this.current++
      }
    },
    isActive (index) {
      return index === this.current
    },
    suggestionClick (index) {
      if(this.localValue.indexOf(this.filtered[index].id)==-1) {
        this.localValue.push(this.filtered[index].id);
      }
      this.$emit('input',this.localValue);
      this.searchValue = '';
      this.open = false;
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
