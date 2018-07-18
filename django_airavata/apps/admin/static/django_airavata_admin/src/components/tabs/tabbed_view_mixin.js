export default {
  data: function () {
    return {
      'tabs': [],
      currentActiveTab: 0,
      previousActiveTab: -1
    }
  },
  computed: {
    tabNames: function () {
      return this.tabs.map((value => value.name))
    }
  },
  methods: {
    tabEventHandler: function (currentIndex, previousIndex) {
      if (this.currentActiveTab != currentIndex) {
        this.previousActiveTab = this.currentActiveTab
        this.currentActiveTab = currentIndex
      }
    }
  }
}

