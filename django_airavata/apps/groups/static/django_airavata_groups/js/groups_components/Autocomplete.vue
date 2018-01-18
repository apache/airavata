<template>
    <div style="position:relative" v-bind:class="{'open':openSuggestion}">
        <span class="selected-cards" style="position:relative" v-for="item in selected" v-bind:key="item.id">
          <b-button disabled variant="warning">
             {{ item.name }}&nbsp;&nbsp;<b-badge variant="light"><a href="#" @click="removeClick(item)">x</a></b-badge>
          </b-button>&nbsp;&nbsp;
        </span>
        <hr>
        <input class="form-control" type="text" :value="value" @input="updateValue($event.target.value)"
          @keydown.enter = 'enter'
          @keydown.down = 'down'
          @keydown.up = 'up'
        >
        <b-list-group style="width: 100%;" v-if="open">
            <b-list-group-item v-for="(suggestion, index) in matches.slice(0,5)" v-bind:class="{'active': isActive(index)}" href="#" @click="suggestionClick(index)" v-bind:key="suggestion.id">
              {{ suggestion.name }}
            </b-list-group-item>
        </b-list-group>
    </div>
</template>

<script>

export default {

  props: {
    value: {
      type: String,
      required: true
    },

    suggestions: {
      type: Array,
      required: true
    }
  },

  data () {
    return {
      open: false,
      current: 0,
      selected: [],
    }
  },

  computed: {
    matches () {
      return this.suggestions.filter((data) => {
        return data.name.indexOf(this.value) >= 0
      })
    },
    openSuggestion () {
      return this.selection !== '' &&
             this.matches.length !== 0 &&
             this.open === true
    },
  },
  methods: {
    updateValue (value) {
      if (this.open === false) {
        this.open = true
        this.current = 0
      }
      if(value===''){
        this.open = false;
      }
      this.$emit('input', value)
    },
    enter () {
      // this.$emit('input', this.matches[this.current].name)
      this.$emit('input','');
      var index = this.suggestions.indexOf(this.matches[this.current].name);
      if(this.selected.indexOf(this.matches[this.current])==-1){
        this.selected.push(this.matches[this.current]);
      }
      this.$emit('updateSelected',this.selected);
      this.open = false
    },
    up () {
      if (this.current > 0) {
        this.current--
      }
    },
    down () {
      if (this.current < this.matches.length - 1) {
        this.current++
      }
    },
    isActive (index) {
      return index === this.current
    },
    suggestionClick (index) {
      // this.$emit('input', this.matches[index].name)
      this.$emit('input','');
      if(this.selected.indexOf(this.matches[index])==-1) {
        this.selected.push(this.matches[index]);
      }
      this.$emit('updateSelected',this.selected);
      this.open = false;
    },
    removeClick(data) {
      var index = this.selected.indexOf(data);
      this.selected.splice(index,1);
    }
  }
}

</script>
