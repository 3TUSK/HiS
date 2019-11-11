new Vue({
  el: "#app",
  data: {
    repost: -1,
    original: -1,
    loaded: false
  },
  computed: {
    ratio: function () {
      if (this.repost <= 0 || this.original <= 0) {
        return "unknown";
      } else {
        return this.repost / this.original;
      }
    },
    timestamp: function () {
      return new Date(Math.floor(Date.now() / 600000) * 600000).toUTCString();
    }
  },
  created: function () {
    fetch("json").then(data => data.json()).then(json => {
      this.repost = json.repost;
      this.original = json.original;
      this.loaded = true;
    });
  }
})