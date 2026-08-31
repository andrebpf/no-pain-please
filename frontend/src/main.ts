import { ApplicationConfig, isDevMode, provideZoneChangeDetection } from '@angular/core';
import { bootstrapApplication } from '@angular/platform-browser';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { provideServiceWorker } from '@angular/service-worker';
import { provideFirebaseApp, initializeApp } from '@angular/fire/app';
import { provideAuth, connectAuthEmulator, getAuth } from '@angular/fire/auth';
import { AppComponent } from './app/app.component';
import { firebaseTokenInterceptor } from './app/firebase-token.interceptor';
import { environment } from './environments/environment';

const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideHttpClient(withInterceptors([firebaseTokenInterceptor])),
    provideFirebaseApp(() => initializeApp(environment.firebase)),
    provideAuth(() => { const auth = getAuth(); if (environment.firebaseEmulator) connectAuthEmulator(auth, environment.firebaseEmulator.authUrl); return auth; }),
    provideServiceWorker('ngsw-worker.js', { enabled: !isDevMode(), registrationStrategy: 'registerWhenStable:30000' })
  ]
};

bootstrapApplication(AppComponent, appConfig).catch(console.error);
