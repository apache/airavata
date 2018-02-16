<template>
    <tr>
        <td>{{ group.name }}</td>
        <td>{{ group.ownerId }}</td>
        <td>{{ group.description }}</td>
        <td><span v-html="action"></span></td>
    </tr>
</template>

<script>
import moment from 'moment';

export default {
    name: 'group-list-item',
    props: ['group', 'type'],
    computed: {
      action: function() {
          // TODO: use (isOwner || isAdmin) here
          if(this.type == 'owner') {
            let deleteUrl = "/api/deleteGroup/"+this.group.groupID;
            let threeSpace = '&nbsp;&nbsp;&nbsp;'
            return '<a href="#">Edit <i class="fa fa-pencil"></i></a>'+threeSpace+'<a href="'+deleteUrl+'">Delete <i class="fa fa-trash"></i></a>';
          }
          else if(this.type == 'member') {
            return '<a href="#">Leave <i class="fa fa-sign-out"></i></a>'
          }
          else {
            return '';
          }
      },
    }
}
</script>

<style>
</style>
