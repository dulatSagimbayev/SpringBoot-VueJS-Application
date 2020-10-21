<template>
    <span></span>
</template>

<script>
    import { mapActions } from 'vuex'
    export default {
        name: 'LazyLoader',
        methods: mapActions(['loadPageAction']),
        mounted() {
            window.onscroll = () => {
                let isBottom =(window.innerHeight + window.pageYOffset) >= document.body.offsetHeight - 2
                if (isBottom==true) {
                    this.loadPageAction()
                }
            }
        },
        beforeDestroy() {
            window.onscroll = null
        }
    }
</script>

<style scoped>
</style>