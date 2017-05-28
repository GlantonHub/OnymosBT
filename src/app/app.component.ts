import { ChangeDetectorRef, Component, ViewChild } from '@angular/core';
import { Nav, Platform } from 'ionic-angular';
import { StatusBar, Splashscreen } from 'ionic-native';
import { Events } from 'ionic-angular';

import { Home } from '../pages/home/home';
import { Account } from '../pages/account/account';
import { Chat } from '../pages/chat/chat';
import { Invite } from '../pages/invite/invite';
import { Login } from '../pages/login/login';

declare var OnymosAccess:any;
declare var OnymosChat:any;
declare var OnymosContacts:any;
declare var OnymosMedia:any;
declare var OnymosUtil:any;

@Component({
	templateUrl: 'app.html'
})
export class MyApp {
	@ViewChild(Nav) nav: Nav;

	rootPage: any = Home;
	authenticated: boolean;

	pages: Array<{title: string, component: any}>;

	constructor(public events: Events, public platform: Platform, private cdRef: ChangeDetectorRef) {
		this.initializeApp();

		this.pages = [
			{ title: 'Home',		component: Home },
			{ title: 'Chat',		component: Chat },
			{ title: 'Invite',	component: Invite },
			{ title: 'Account',	component: Account }
		];

		this.authenticated = false;

	} /* end constructor */

	initializeApp() {
		this.platform.ready().then(() => {
			
			this.events.subscribe('user:login', (time) => {
				this.authenticated = true;
				this.cdRef.detectChanges();
			});

			this.events.subscribe('user:logout', (time) => {
				this.authenticated = false;
				this.cdRef.detectChanges();
				this.nav.setRoot(Home);
			});

			this.initializeOnymosComponents();

		});
	} /* end function initializeApp */

	initializeOnymosComponents() {
		let onymosConnectObj = {
			customerId : 'OXXXXXXXXXX', // Obtain at Account > Settings > Access Keys
			onymosAuthToken : 'SuperLongSecretKey',  // Obtain at Account > Settings > Access Keys
			envType : 'PRD'
		};

		let that = this;
		let numberOfOnymosComponentsInitialized = 0;

		/* Initialize Access Component */
		OnymosAccess.initialize (
			onymosConnectObj,

			function onymosInitializeSuccess (status) {
				console.log('app.component.ts : OnymosAccess.initialize status - ' + status);
				numberOfOnymosComponentsInitialized++;

				if (OnymosAccess.getAuth()) {
					that.events.publish('user:login', Date.now());
				}
				else {
					that.events.publish('user:logout', Date.now());
				}

				if (numberOfOnymosComponentsInitialized === 5) {
					StatusBar.styleDefault();
					Splashscreen.hide();
				}

			},

			function onymosInitializeFailure (error) {
				console.log('app.component.ts : OnymosAccess.initialize error - ' + error);

			}); /* end OnymosAccess.initialize */
		/* end Initialize Access Component */

		/* Initialize Chat Component */
		OnymosChat.initialize (
			onymosConnectObj,

			function onymosInitializeSuccess (status) {
				console.log('app.component.ts : OnymosChat.initialize status - ' + status);
				numberOfOnymosComponentsInitialized++;

				if (numberOfOnymosComponentsInitialized === 5) {
					StatusBar.styleDefault();
					Splashscreen.hide();
				}

			},

			function onymosInitializeFailure (error) {
				console.log('app.component.ts : OnymosChat.initialize error - ' + error);

			}); /* end OnymosChat.initialize */
		/* end Initialize Chat Component */

		/* Initialize Contacts Component */
		OnymosContacts.initialize (
			onymosConnectObj,

			function onymosInitializeSuccess (status) {
				console.log('app.component.ts : OnymosContacts.initialize status - ' + status);
				numberOfOnymosComponentsInitialized++;

				if (numberOfOnymosComponentsInitialized === 5) {
					StatusBar.styleDefault();
					Splashscreen.hide();
				}

			},

			function onymosInitializeFailure (error) {
				console.log('app.component.ts : OnymosContacts.initialize error - ' + error);

			}); /* end OnymosContacts.initialize */
		/* end Initialize Contacts Component */

		/* Initialize Media Component */
		OnymosMedia.onymosInitialize (
			onymosConnectObj,

			function onymosInitializeSuccess (status) {
				console.log('app.component.ts : OnymosMedia.onymosInitialize status - ' + status);
				numberOfOnymosComponentsInitialized++;

				if (numberOfOnymosComponentsInitialized === 5) {
					StatusBar.styleDefault();
					Splashscreen.hide();
				}

			},

			function onymosInitializeFailure (error) {
				console.log('app.component.ts : OnymosMedia.onymosInitialize error - ' + error);

			}); /* end OnymosMedia.onymosInitialize */
		/* end Initialize Media Component */

		/* Initialize Util Component */
		OnymosUtil.initialize (
			onymosConnectObj,

			function onymosInitializeSuccess (status) {
				console.log('app.component.ts : OnymosUtil.initialize status - ' + status);
				numberOfOnymosComponentsInitialized++;

				if (numberOfOnymosComponentsInitialized === 5) {
					StatusBar.styleDefault();
					Splashscreen.hide();
				}

			},

			function onymosInitializeFailure (error) {
				console.log('app.component.ts : OnymosUtil.initialize error - ' + error);

			}); /* end OnymosUtil.initialize */
		/* end Initialize Util Component */

	} /* end function initializeOnymosComponents */


	navigateTo (page) {
		this.nav.setRoot(page.component)
			.catch(() => {

				// Page requires authentication, re-direct to Login page
				this.nav.setRoot(Login, {routeToPage: page.title});

			});

	} /* end function navigateTo */

	socialLogin() {
		this.nav.setRoot(Login);

	} /* end function socialLogin */

	socialLogout() {

		let that = this;

		OnymosAccess.logout(
			function logoutSuccess (statusMessage) {
				console.log('app.component.ts : onymosAccessLogout - Success');
				that.events.publish('user:logout', Date.now());
			},

			function logoutFailure (error) {
				console.log('app.component.ts : onymosAccessLogout error - ' + error);
			});

	} /* end function socialLogout */

} /* end class MyApp */
